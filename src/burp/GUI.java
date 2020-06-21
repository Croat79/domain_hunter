package burp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.net.InternetDomainName;

public class GUI extends JFrame {

	public static DomainObject domainResult = new DomainObject("");

	private JRadioButton rdbtnAddRelatedToRoot;
	private DefaultTableModel tableModel; 

	private JPanel contentPane;
	private JTextField textFieldUploadURL;
	private JButton btnSearch;
	private JButton btnUpload;
	private JButton btnCrawl;
	private JLabel lblSummary;
	private JPanel FooterPanel;
	private JLabel lblNewLabel_2;
	private JScrollPane TargetPanel;
	private JTextArea textAreaSubdomains;
	private JTextArea textAreaSimilarDomains;

	private int sortedColumn;
	private SortOrder sortedMethod;
	private JTable table;
	private JPanel panel;
	private JButton RemoveButton;
	private JButton AddButton;
	private JSplitPane TargetSplitPane;
	private JTextArea textAreaRelatedDomains;
	private JButton btnSave;
	private JButton btnOpen;
	private Component verticalStrut;
	private Component verticalStrut_1;
	private JButton btnCopy;
	private JButton btnNew;

	public PrintWriter stdout;
	public PrintWriter stderr;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1174, 497);
		contentPane =  new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);


		stdout = new PrintWriter(System.out, true);
		stderr = new PrintWriter(System.out, true);
		///////////////////////HeaderPanel//////////////


		JPanel HeaderPanel = new JPanel();
		FlowLayout fl_HeaderPanel = (FlowLayout) HeaderPanel.getLayout();
		fl_HeaderPanel.setAlignment(FlowLayout.LEFT);
		contentPane.add(HeaderPanel, BorderLayout.NORTH);


		btnNew = new JButton("New");
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(null,"Save Current Project?");

				/*     是:   JOptionPane.YES_OPTION
				 *     否:   JOptionPane.NO_OPTION
				 *     取消: JOptionPane.CANCEL_OPTION
				 *     关闭: JOptionPane.CLOSED_OPTION*/
				if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
					return;
				}else if (result == JOptionPane.YES_OPTION) {
					saveDialog();
				}else if (result == JOptionPane.NO_OPTION) {
					// nothing to do
				}

				String projectName = JOptionPane.showInputDialog("Enter Name For New Project", null);
				domainResult = new DomainObject(projectName);
				showToUI(domainResult);

			}
		});
		btnNew.setToolTipText("Create A New Project");
		HeaderPanel.add(btnNew);



		btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc=new JFileChooser();
				JsonFileFilter jsonFilter = new JsonFileFilter(); //excel过滤器  
				fc.addChoosableFileFilter(jsonFilter);
				fc.setFileFilter(jsonFilter);
				fc.setDialogTitle("Chose Domain Hunter Project File");
				fc.setDialogType(JFileChooser.CUSTOM_DIALOG);
				if(fc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
					try {
						File file=fc.getSelectedFile();
						String contents = Files.toString(file, Charsets.UTF_8);
						domainResult = domainResult.Open(contents);
						stdout.println("open project ["+domainResult.projectName+"] in "+ file.getName());
						//List<String> lines = Files.readLines(file, Charsets.UTF_8);
						showToUI(domainResult);

					} catch (IOException e1) {
						e1.printStackTrace(stderr);
					}
				}
			}
		});
		btnOpen.setToolTipText("Open Domain Hunter Project File");
		HeaderPanel.add(btnOpen);

		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveDialog();
			}});
		btnSave.setToolTipText("Save Domain Hunter Project File");
		HeaderPanel.add(btnSave);


		btnSearch = new JButton("Search");
		btnSearch.setToolTipText("Do a single search from site map");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker<Map, Map> worker = new SwingWorker<Map, Map>() {
					//using SwingWorker to prevent blocking burp main UI.

					@Override
					protected Map doInBackground() throws Exception {

						Set<String> rootDomains = domainResult.fetchRootDomainSet();
						Set<String> keywords= domainResult.fetchKeywordSet();

						//stderr.print(keywords.size());
						//System.out.println(rootDomains.toString());
						//System.out.println("xxx"+keywords.toString());
						btnSearch.setEnabled(false);
						return search(rootDomains,keywords);
					}
					@Override
					protected void done() {
						try {
							Map result = get();				        	
							showToUI(domainResult);
							btnSearch.setEnabled(true);
						} catch (Exception e) {
							btnSearch.setEnabled(true);
							e.printStackTrace(stderr);
						}
					}
				};      
				worker.execute();

			}
		});

		verticalStrut = Box.createVerticalStrut(20);
		HeaderPanel.add(verticalStrut);
		HeaderPanel.add(btnSearch);

		btnCrawl = new JButton("Crawl");
		btnCrawl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				SwingWorker<Map, Map> worker = new SwingWorker<Map, Map>() {
					//可以在一个类中实现另一个类，直接实现原始类，没有变量处理的困扰；
					//之前的想法是先单独实现一个worker类，在它里面处理各种，就多了一层实现，然后在这里调用，变量调用会是一个大问题。
					//https://stackoverflow.com/questions/19708646/how-to-update-swing-ui-while-actionlistener-is-in-progress
					@Override
					protected Map doInBackground() throws Exception {                
						domainResult.rootDomainMap =getTableMap();
						Set<String> rootDomains = domainResult.fetchRootDomainSet();
						Set<String> keywords= domainResult.fetchKeywordSet();

						btnCrawl.setEnabled(false);
						return crawl(rootDomains,keywords);

					}
					@Override
					protected void done() {
						try {
							Map result = get();
							showToUI(domainResult);
							btnCrawl.setEnabled(true);
						} catch (Exception e) {
							e.printStackTrace(stderr);
						}
					}
				};
				worker.execute();
			}
		});
		btnCrawl.setToolTipText("Crawl all subdomains recursively,This may take a long time and large Memory Usage!!!");
		HeaderPanel.add(btnCrawl);

		verticalStrut_1 = Box.createVerticalStrut(20);
		HeaderPanel.add(verticalStrut_1);

		textFieldUploadURL = new JTextField("Input Upload URL Here");
		textFieldUploadURL.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (textFieldUploadURL.getText().equals("Input Upload URL Here")) {
					textFieldUploadURL.setText("");
				}
			}
			@Override
			public void focusLost(FocusEvent e) {
				if (textFieldUploadURL.getText().equals("")) {
					textFieldUploadURL.setText("Input Upload URL Here");
				}

			}
		});
		HeaderPanel.add(textFieldUploadURL);
		textFieldUploadURL.setColumns(30);


		btnUpload = new JButton("Upload");
		btnUpload.setToolTipText("Do a single search from site map");
		btnUpload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker<Boolean, Boolean> worker = new SwingWorker<Boolean, Boolean>() {
					@Override
					protected Boolean doInBackground() throws Exception {
						return upload(domainResult.uploadURL,domainResult.Save());
					}
					@Override
					protected void done() {
						//TODO
					}
				};  
				worker.execute();
			}
		});
		HeaderPanel.add(btnUpload);

		lblSummary = new JLabel("      ^_^");
		HeaderPanel.add(lblSummary);


		////////////////////////////////////target area///////////////////////////////////////////////////////


		TargetPanel = new JScrollPane();
		TargetPanel.setViewportBorder(new LineBorder(new Color(0, 0, 0)));
		//contentPane.add(TargetPanel, BorderLayout.WEST);

		table = new JTable();
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setBorder(new LineBorder(new Color(0, 0, 0)));

		table.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					sortedColumn = table.getRowSorter().getSortKeys().get(0).getColumn();
					//System.out.println(sortedColumn);
					sortedMethod = table.getRowSorter().getSortKeys().get(0).getSortOrder();
					System.out.println(sortedMethod); //ASCENDING   DESCENDING
				} catch (Exception e1) {
					sortedColumn = -1;
					sortedMethod = null;
					e1.printStackTrace(stderr);
				}
			}
		});

		tableModel = new DefaultTableModel(
				new Object[][] {
					//{"1", "1","1"},
				},
				new String[] {
						"Root Domain", "Keyword"//, "Source"
				}
				);
		table.setModel(tableModel);
		tableModel.addTableModelListener(new TableModelListener(){
			@Override
			public void tableChanged(TableModelEvent e) {
				domainResult.rootDomainMap = getTableMap();
			}
		});



		RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
		table.setRowSorter(sorter);

		table.setColumnSelectionAllowed(true);
		table.setCellSelectionEnabled(true);
		table.setSurrendersFocusOnKeystroke(true);
		table.setFillsViewportHeight(true);
		table.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		TargetPanel.setViewportView(table);

		JSplitPane CenterSplitPane = new JSplitPane();
		CenterSplitPane.setResizeWeight(0.5);
		contentPane.add(CenterSplitPane, BorderLayout.CENTER);


		JSplitPane leftOfCenterSplitPane = new JSplitPane();
		leftOfCenterSplitPane.setResizeWeight(0.5);
		CenterSplitPane.setLeftComponent(leftOfCenterSplitPane);


		JSplitPane rightOfCenterSplitPane = new JSplitPane();//右半部分
		rightOfCenterSplitPane.setResizeWeight(0.5);
		CenterSplitPane.setRightComponent(rightOfCenterSplitPane);

		JSplitPane TargetSplitPane = new JSplitPane();//1/4
		TargetSplitPane.setResizeWeight(0.5);
		TargetSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		leftOfCenterSplitPane.setLeftComponent(TargetSplitPane);

		TargetSplitPane.setLeftComponent(TargetPanel);


		///////////////////////////////Target Operations and Config//////////////////////


		panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		TargetSplitPane.setRightComponent(panel);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));


		AddButton = new JButton("Add");
		AddButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String enteredRootDomain = JOptionPane.showInputDialog("Enter Root Domain", null);
				enteredRootDomain = enteredRootDomain.trim();
				enteredRootDomain =InternetDomainName.from(enteredRootDomain).topPrivateDomain().toString();
				String keyword = enteredRootDomain.substring(0,enteredRootDomain.indexOf("."));

				domainResult.AddToRootDomainMap(enteredRootDomain, keyword);
				showToUI(domainResult);


				/*				if (domainResult.rootDomainMap.containsKey(enteredRootDomain) && domainResult.rootDomainMap.containsValue(keyword)) {
					//do nothing
				}else {
					domainResult.rootDomainMap.put(enteredRootDomain,keyword);
					showToUI(domainResult);
				}*/
			}
		});
		panel.add(AddButton);


		RemoveButton = new JButton("Remove");
		panel.add(RemoveButton);
		RemoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int[] rowindexs = table.getSelectedRows();
				for (int i=0; i < rowindexs.length; i++){
					rowindexs[i] = table.convertRowIndexToModel(rowindexs[i]);//转换为Model的索引，否则排序后索引不对应。
				}
				Arrays.sort(rowindexs);

				tableModel = (DefaultTableModel) table.getModel();
				for(int i=rowindexs.length-1;i>=0;i--){
					tableModel.removeRow(rowindexs[i]);
				}
				// will trigger tableModel listener

				domainResult.rootDomainMap = getTableMap();
				showToUI(domainResult);
			}
		});


		btnCopy = new JButton("Copy");
		btnCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection selection = new StringSelection(domainResult.fetchRootDomains());
				clipboard.setContents(selection, null);

			}
		});

		btnCopy.setToolTipText("Copy Root Domains To ClipBoard");
		panel.add(btnCopy);


		rdbtnAddRelatedToRoot = new JRadioButton("Auto Add Related Domain To Root Domain");
		rdbtnAddRelatedToRoot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				domainResult.autoAddRelatedToRoot = rdbtnAddRelatedToRoot.isSelected();
				if (domainResult.autoAddRelatedToRoot==true) {
					domainResult.relatedToRoot();
					showToUI(domainResult);/*
					Set<String> tableRootDomains = getColumnValues("Root Domain");
					for(String relatedDomain:domainResult.relatedDomainSet) {
			        	String rootDomain =InternetDomainName.from(relatedDomain).topPrivateDomain().toString();
						String keyword = rootDomain.substring(0,rootDomain.indexOf("."));
						if (!tableRootDomains.contains(rootDomain)) {
							tableModel.addRow(new Object[]{rootDomain,keyword});
						}
						//after this, tableModelListener will auto update rootDomainMap.
					}

					for (String similarDomain:domainResult.similarDomainSet) {
						String rootDomain =InternetDomainName.from(similarDomain).topPrivateDomain().toString();
						if (domainResult.rootDomainMap.keySet().contains(rootDomain)) {
							domainResult.subDomainSet.add(similarDomain);
							domainResult.similarDomainSet.remove(similarDomain);
						}
					}*/
				}
			}
		});
		rdbtnAddRelatedToRoot.setSelected(false);
		panel.add(rdbtnAddRelatedToRoot);


		///////////////////////////////textAreas///////////////////////////////////////////////////////


		JScrollPane ScrollPaneRelatedDomains = new JScrollPane();
		JScrollPane ScrollPaneSubdomains = new JScrollPane();
		JScrollPane ScrollPaneSimilarDomains = new JScrollPane();


		leftOfCenterSplitPane.setRightComponent(ScrollPaneRelatedDomains);
		rightOfCenterSplitPane.setLeftComponent(ScrollPaneSubdomains);
		rightOfCenterSplitPane.setRightComponent(ScrollPaneSimilarDomains);

		textAreaRelatedDomains = new JTextArea();
		textAreaSubdomains = new JTextArea();
		textAreaSimilarDomains = new JTextArea();

		ScrollPaneRelatedDomains.setViewportView(textAreaRelatedDomains);
		ScrollPaneSubdomains.setViewportView(textAreaSubdomains);
		ScrollPaneSimilarDomains.setViewportView(textAreaSimilarDomains);


		textAreaRelatedDomains.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				Set<String> domainList = new HashSet<>(Arrays.asList(textAreaRelatedDomains.getText().split(System.lineSeparator())));
				domainList.remove("");
				domainResult.setRelatedDomainSet(domainList);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				Set<String> domainList = new HashSet<>(Arrays.asList(textAreaRelatedDomains.getText().split(System.lineSeparator())));
				domainList.remove("");
				domainResult.setRelatedDomainSet(domainList);
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				Set<String> domainList = new HashSet<>(Arrays.asList(textAreaRelatedDomains.getText().split(System.lineSeparator())));
				domainList.remove("");
				domainResult.setRelatedDomainSet(domainList);
			}
		});

		textAreaSubdomains.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				Set<String> domainList = new HashSet<>(Arrays.asList(textAreaSubdomains.getText().split(System.lineSeparator())));
				domainList.remove("");
				domainResult.setSubDomainSet(domainList);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				Set<String> domainList = new HashSet<>(Arrays.asList(textAreaSubdomains.getText().split(System.lineSeparator())));
				domainList.remove("");
				domainResult.setSubDomainSet(domainList);
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				Set<String> domainList = new HashSet<>(Arrays.asList(textAreaSubdomains.getText().split(System.lineSeparator())));
				domainList.remove("");
				domainResult.setSubDomainSet(domainList);
			}
		});

		textAreaSimilarDomains.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				Set<String> domainList = new HashSet<>(Arrays.asList(textAreaSimilarDomains.getText().split(System.lineSeparator())));
				domainList.remove("");
				domainResult.setSimilarDomainSet(domainList);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				Set<String> domainList = new HashSet<>(Arrays.asList(textAreaSimilarDomains.getText().split(System.lineSeparator())));
				domainList.remove("");
				domainResult.setSimilarDomainSet(domainList);
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				Set<String> domainList = new HashSet<>(Arrays.asList(textAreaSimilarDomains.getText().split(System.lineSeparator())));
				domainList.remove("");
				domainResult.setSimilarDomainSet(domainList);
			}
		});

		textAreaSubdomains.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				final JPopupMenu jp = new JPopupMenu();
				jp.add("^_^");
				textAreaSubdomains.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getButton() == MouseEvent.BUTTON3) {
							// 弹出菜单
							jp.show(textAreaSubdomains, e.getX(), e.getY());
						}
					}
				});
			}
		});
		textAreaSubdomains.setColumns(30);

		///////////////////////////FooterPanel//////////////////


		FooterPanel = new JPanel();
		FlowLayout fl_FooterPanel = (FlowLayout) FooterPanel.getLayout();
		fl_FooterPanel.setAlignment(FlowLayout.LEFT);
		contentPane.add(FooterPanel, BorderLayout.SOUTH);

		lblNewLabel_2 = new JLabel(BurpExtender.getFullExtensionName()+"    "+BurpExtender.github);
		lblNewLabel_2.setFont(new Font("宋体", Font.BOLD, 12));
		lblNewLabel_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					URI uri = new URI(BurpExtender.github);
					Desktop desktop = Desktop.getDesktop();
					if(Desktop.isDesktopSupported()&&desktop.isSupported(Desktop.Action.BROWSE)){
						desktop.browse(uri);
					}
				} catch (Exception e2) {
					e2.printStackTrace(stderr);
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				lblNewLabel_2.setForeground(Color.BLUE);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				lblNewLabel_2.setForeground(Color.BLACK);
			}
		});
		FooterPanel.add(lblNewLabel_2);
	}


	//////////////////////////////methods//////////////////////////////////////
	public Map<String, Set<String>> crawl (Set<String> rootdomains, Set<String> keywords) {
		System.out.println("spiderall testing... you need to over write this function!");
		return null;
	}


	public Map<String, Set<String>> search(Set<String> rootdomains, Set<String> keywords){
		System.out.println("search testing... you need to over write this function!");
		return null;
	}

	public Boolean upload(String url,String content) {
		if ((url.toLowerCase().contains("http://") ||url.toLowerCase().contains("https://"))
				&& content != null){
			try {
				HTTPPost.httpPostRequest(url,content);
				return true;
			} catch (IOException e) {
				e.printStackTrace(stderr);
				return false;
			}
		}
		return false;
	}


	public Set<String> getColumnValues(String ColumnName) {

		Set<String> result = new HashSet<String>();
		int index=-1;
		for (int i=0;i<table.getColumnCount();i++) {
			if (table.getColumnName(i).equals(ColumnName)) {
				index = i;
				break;
			}
		}

		for(int j=0;j<table.getRowCount();j++){
			String onecell ="";
			try {
				onecell= (String)table.getValueAt(j,index);
			}catch(Exception e) {

			}

			if (!onecell.equals("") && !onecell.equals(null)) {
				result.add(onecell);
			}
		}
		return result;
	}

	public LinkedHashMap<String, String> getTableMap() {
		LinkedHashMap<String,String> tableMap= new LinkedHashMap<String,String>();

		/*		for(int x=0;x<table.getRowCount();x++){
			String key =(String) table.getValueAt(x, 0);
			String value = (String) table.getValueAt(x, 1); //encountered a "ArrayIndexOutOfBoundsException" error here~~ strange!
			tableMap.put(key,value);
		}
		return tableMap;*/

		Vector data = tableModel.getDataVector();
		for (Object o : data) {
			Vector v = (Vector) o;
			String key = (String) v.elementAt(0);
			String value = (String) v.elementAt(1);
			if (key != null && value != null) {
				tableMap.put(key, value);
			}
		}
		return tableMap;
	}

	public void ClearTable() {
		LinkedHashMap<String, String> tmp = domainResult.rootDomainMap;

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setRowCount(0);
		//this also trigger tableModel listener. lead to rootDomainMap to empty!!
		//so need to backup rootDomainMap and restore!
		domainResult.rootDomainMap = tmp;
	}

	public void showToUI(DomainObject domainResult) {

		domainResult.relatedToRoot();
		ClearTable();

		for (Entry<String, String> entry:domainResult.rootDomainMap.entrySet()) {
			tableModel.addRow(new Object[]{entry.getKey(),entry.getValue()});
		}

		textFieldUploadURL.setText(domainResult.uploadURL);
		textAreaSubdomains.setText(domainResult.fetchSubDomains());
		textAreaSimilarDomains.setText(domainResult.fetchSimilarDomains());
		textAreaRelatedDomains.setText(domainResult.fetchRelatedDomains());
		lblSummary.setText(domainResult.getSummary());
		rdbtnAddRelatedToRoot.setSelected(domainResult.autoAddRelatedToRoot);
	}

	public void saveDialog() {
		JFileChooser fc=new JFileChooser();
		JsonFileFilter jsonFilter = new JsonFileFilter(); //excel过滤器  
		fc.addChoosableFileFilter(jsonFilter);
		fc.setFileFilter(jsonFilter);
		fc.setDialogTitle("Save Domain Hunter file:");
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		if(fc.showSaveDialog(null)==JFileChooser.APPROVE_OPTION){
			File file=fc.getSelectedFile();

			if(!(file.getName().toLowerCase().endsWith(".json"))){
				file=new File(fc.getCurrentDirectory(),file.getName()+".json");
			}

			if (domainResult.projectName == "") {
				domainResult.projectName = file.getName();
			}


			String content= domainResult.Save();
			try{
				if(file.exists()){
					int result = JOptionPane.showConfirmDialog(null,"Are you sure to overwrite this file ?");
					if (result == JOptionPane.YES_OPTION) {
						file.createNewFile();
					}else {
						return;
					}
				}else {
					file.createNewFile();
				}

				Files.write(content.getBytes(), file);
			}catch(Exception e1){
				e1.printStackTrace(stderr);
			}
		}
	}


	class JsonFileFilter extends FileFilter {
		public String getDescription() {  
			return "*.json";  
		}  

		public boolean accept(File file) {  
			String name = file.getName();  
			return file.isDirectory() || name.toLowerCase().endsWith(".json");  // 仅显示目录和json文件
		}
	}


	public static DomainObject getDomainResult() {
		// TODO Auto-generated method stub
		return domainResult;
	}

}
