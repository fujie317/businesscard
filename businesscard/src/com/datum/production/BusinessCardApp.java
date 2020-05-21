package com.datum.production;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import edu.stanford.nlp.simple.Sentence;

public class BusinessCardApp extends JFrame implements ActionListener {

	private static final long serialVersionUID = 9185713514995202694L;
	static final int CHOICES = 12;
	private Map<String, String> dict = new HashMap<>();

	public BusinessCardApp() {
		super("Business Card Data Verification");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setSize(2000, 1000);
		getRootPane().setBorder(BorderFactory.createEtchedBorder());
		setLocation(20, 40);

		// Initialize Dictionary
		BufferedReader fr = null;
		String line = null;
		try {
			/*
			 * fr = new BufferedReader(new FileReader(
			 * getClass().getClassLoader().getResource("files/EnglishChineseDictionary.txt")
			 * .getFile()));
			 */
			InputStream in = new FileInputStream(new File("files/EnglishChineseDictionary.txt")); 
			fr = new BufferedReader(new InputStreamReader(in, "UTF8")); 
			
			while ((line = fr.readLine()) != null) {
				int first = line.indexOf(':');
				String key = line.substring(0, first);
				String def = line.length() > first + 32 ? line.substring(first + 2, first + 32)
						: line.substring(first + 2);
				dict.put(key, def);
			}

		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (fr != null)
				try {
					fr.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
		// Main menu
		JMenuBar mb = new JMenuBar();
		mb.setMargin(new Insets(20, 20, 20, 20));
		mb.setPreferredSize(new Dimension(100, 40));
		mb.setBorder(BorderFactory.createBevelBorder(CHOICES, getForeground(), getForeground()));
		Font f = new Font("sans-serif", Font.BOLD, 16);
		UIManager.put("Menu.font", f);
		JMenu file = new JMenu("File", true);
		file.setMargin(new Insets(20, 20, 20, 20));
		file.add(Box.createVerticalStrut(5));
		JMenuItem open = new JMenuItem("Open");
		open.setMargin(new Insets(20, 20, 20, 20));
		file.add(open);
		file.add(Box.createVerticalStrut(10));
		JMenuItem close = new JMenuItem("Close");
		close.setMargin(new Insets(20, 20, 20, 20));
		file.add(close);
		file.add(Box.createVerticalStrut(10));
		JMenuItem save = new JMenuItem("Save");
		save.setMargin(new Insets(20, 20, 20, 20));
		file.add(save);
		file.add(Box.createVerticalStrut(10));
		JMenuItem saveAs = new JMenuItem("Save As");
		saveAs.setMargin(new Insets(20, 20, 20, 20));
		file.add(saveAs);
		file.add(Box.createVerticalStrut(10));
		JMenuItem quit = new JMenuItem("Quit");
		quit.setMargin(new Insets(20, 20, 20, 20));
		quit.addActionListener(this);
		file.add(quit);
		file.add(Box.createVerticalStrut(5));

		JMenu help = new JMenu("Help", true);
		help.setMargin(new Insets(20, 20, 20, 20));
		help.add(Box.createVerticalStrut(5));
		JMenuItem tut = new JMenuItem("Tutorial");
		tut.setMargin(new Insets(20, 20, 20, 20));
		help.add(tut);
		help.add(Box.createVerticalStrut(10));
		JMenuItem about = new JMenuItem("About");
		about.setMargin(new Insets(20, 20, 20, 20));
		help.add(about);
		help.add(Box.createVerticalStrut(5));

		this.addWindowListener(closeWindow);

		mb.add(file);
		mb.add(Box.createHorizontalStrut(20));
		mb.add(help);
		setJMenuBar(mb);

		JTabbedPane tabbedPane = new JTabbedPane();
		
		DefaultTableModel mModel = new DefaultTableModel(0, 0);
		JTable mTable = new JTable(mModel) {
			private static final long serialVersionUID = 1L;

			// Implement table cell tool tips.
			public String getToolTipText(MouseEvent e) {
				String tip = null;
				String[] tips = null;
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);
				String toolTipText = "<html> <ul>";
				try {
					tip = getValueAt(rowIndex, colIndex).toString();
					tips = tip.split("\\W+");
					for (String lemma : tips) {
						String word, def;
						word = new String(lemma);
						lemma = lemma.toLowerCase();
						List<String> lemmas = new Sentence(lemma).lemmas();
						for (String tmp : lemmas) {
							if (lemma.length() > tmp.length())
								lemma = tmp;
						}
						if (lemma != null) {
							def = dict.get(lemma);
							if (def != null)
								toolTipText += "<li>" + word + ": " + def + "</li>";
						}
					}
					toolTipText += "</ul> </html>";
				} catch (RuntimeException e1) {
					// catch null pointer exception if mouse is over an empty line
				}
				return toolTipText;
			}
		};
		mTable.setRowSelectionAllowed(true);
		mTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JComponent mPanel = new JScrollPane(mTable);
		mTable.setFillsViewportHeight(true);

		DefaultTableModel dModel = new DefaultTableModel(0, 0);
		JTable dTable = new JTable(dModel);
		dTable.setRowSelectionAllowed(true);
		dTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JComponent dPanel = new JScrollPane(dTable);
		dTable.setFillsViewportHeight(true);

		Map<String, JTable> tables = new HashMap<>();
		tables.put("reader", mTable);
		tables.put("重复可删除", dTable);

		// internal storage for card info entries
		String[] colNames = new String[] { "reader_id", "co_name_eng", "name_eng", "title_code", "title_eng",
				"title_chi", "dept_code", "add_eng1", "add_eng2", "add_eng3" };
		Set<String> visibleHeadings = new HashSet<>();
		for(String s: colNames)
			visibleHeadings.add(s);
		
		CardInfoTable cit = new CardInfoTable(tables, visibleHeadings);
		open.addActionListener(cit);
		close.addActionListener(cit);
		save.addActionListener(cit);
		saveAs.addActionListener(cit);

		tables.get("reader").addMouseListener(cit);
		tables.get("重复可删除").addMouseListener(cit);

		tabbedPane.addTab("reader", null, mPanel, "主表");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.addTab("重复可删除", null, dPanel, "删除数据列表");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		JPanel container = new JPanel(new GridLayout());
		container.add(tabbedPane);
		add(container, BorderLayout.CENTER);
		pack();
		setVisible(true);
		toFront();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		System.exit(0);
	}

	private static WindowListener closeWindow = new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
			e.getWindow().dispose();
		}
	};

	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				UIManager.getLookAndFeelDefaults().put("defaultFont", new Font("Song", 0, 12));
				UIManager.put("Label.font", new Font("Song", 1, 12));
				new BusinessCardApp();
			}
		});

	}

}
