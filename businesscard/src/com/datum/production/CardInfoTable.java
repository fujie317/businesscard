package com.datum.production;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CardInfoTable extends MouseInputAdapter implements ActionListener {

	private File file;
	private Map<String, JTable> tables;
	private Set<String> visibleHeadings;

	public CardInfoTable(Map<String, JTable> tables, Set<String> visibleHeadings) {
		this.tables = tables;
		this.visibleHeadings = visibleHeadings;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Handle open button action.
		String src = ((JMenuItem) e.getSource()).getActionCommand();
		if (src.equalsIgnoreCase("Open")) {
			try {
				populateTables();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			}
		} else if (src.equalsIgnoreCase("Close")) {
			closeTables();
		} else if (src.equalsIgnoreCase("Save")) {
			updateTables(file);
		} else if (src.equalsIgnoreCase("Save As")) {
			JFileChooser fileChooser;
			File newfile = null;
			do {
				fileChooser = new JFileChooser();
				if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
					newfile = fileChooser.getSelectedFile();
				if (newfile.exists())
					JOptionPane.showMessageDialog(null, "File Already Exists! Choose a New One.", "Warning!",
							JOptionPane.WARNING_MESSAGE);
			} while (newfile.exists());
			updateTables(newfile);
			file = newfile;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		DefaultTableModel dModel = (DefaultTableModel) tables.get("重复可删除").getModel();
		Vector<String> rowData = new Vector<>();
		if (SwingUtilities.isRightMouseButton(e)) {
			int clickCount = e.getClickCount();
			if (clickCount == 2) {
				java.awt.Point p = e.getPoint();
				JTable source = (JTable) e.getComponent();
				int rowIndex = source.rowAtPoint(p);
				rowIndex = source.convertRowIndexToModel(rowIndex);
				int columnCount = dModel.getColumnCount();
				rowData.add("重复");
				for (int colnum = 1; colnum < columnCount; ++colnum) {
					try {
						rowData.add(source.getModel().getValueAt(rowIndex, colnum).toString());
					}
					catch(NullPointerException npe) {
						rowData.add("");
					}
				}
				dModel.addRow(rowData);
				((DefaultTableModel) source.getModel()).removeRow(rowIndex);
			}
		}
	}

	@SuppressWarnings("unused")
	private void updateTables(File output) {
		try {
			FileInputStream fis = new FileInputStream(file);
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			int totalSheet = workbook.getNumberOfSheets();
			XSSFSheet sheet = null;
			XSSFRow topRow = null;
			for (int i = 0; i < totalSheet; ++i) {
				sheet = workbook.getSheetAt(i);
				for (String tableName : tables.keySet()) {
					if (sheet.getSheetName().compareTo(tableName) == 0) {
						DefaultTableModel mdl = (DefaultTableModel) tables.get(tableName).getModel();
						XSSFRow row;
						int sheetRowCount = sheet.getLastRowNum();
						int modelRowCount = mdl.getRowCount();
						int maxRowCount = Math.max(sheetRowCount, modelRowCount) + 1;
						int colCount = mdl.getColumnCount();
						for (int rownum = 1; rownum < maxRowCount; ++rownum) {
							if(rownum <= sheetRowCount) {
								row = sheet.getRow(rownum);
								for(int colnum = 0; colnum < colCount; colnum++) {
									if(rownum -1 < modelRowCount) {
										try {
											row.getCell(colnum).setCellValue((String)mdl.getValueAt(rownum-1, colnum));
										}
										catch(NullPointerException npe) {
											continue;
										}
									}
									else {
										try {
											sheet.removeRow(sheet.getRow(rownum));
										}
										catch(NullPointerException npe){
										}
									}
								}
							}
							else {
								row = sheet.createRow(rownum);
								for(int colnum = 0; colnum < colCount; ++colnum) {
									try {
										row.createCell(colnum, CellType.STRING).setCellValue((String)mdl.getValueAt(rownum-1, colnum));
									}
									catch(NullPointerException npe) {
										continue;
									}
								}
							}
						}
					}
				}
			}
			fis.close();
			FileOutputStream fos = new FileOutputStream(output);
			workbook.write(fos);
			workbook.close();
			fos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void closeTables() {
		for (JTable table : tables.values()) {
			if (((DefaultTableModel) table.getModel()).getRowCount() > 0)
				((DefaultTableModel) table.getModel()).setRowCount(0);
		}
	}

	private void populateTables() throws FileNotFoundException {
		closeTables();
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal;
		returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			FileInputStream fis = new FileInputStream(file);
			XSSFWorkbook workbook = null;
			try {
				workbook = new XSSFWorkbook(fis);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

			DataFormatter fmt = new DataFormatter();
			Vector<String> headings = new Vector<>();
			int totalSheet = workbook.getNumberOfSheets();
			XSSFSheet sheet = null;
			XSSFRow topRow = null;
			for (int i = 0; i < totalSheet; ++i) {
				sheet = workbook.getSheetAt(i);
				topRow = sheet.getRow(0);
				for (Cell c : topRow) {
					headings.add(fmt.formatCellValue(c));
				}
				for (String tableName : tables.keySet()) {
					if (sheet.getSheetName().compareTo(tableName) == 0) {
						((DefaultTableModel) tables.get(tableName).getModel())
								.setColumnIdentifiers(headings.toArray());
						Vector<String> rowData = null;
						XSSFRow row = null;
						for (int rownum = 1; rownum <= sheet.getLastRowNum(); rownum++) {
							rowData = new Vector<String>();
							row = sheet.getRow(rownum);
							for (int colnum = row.getFirstCellNum(); colnum < row.getLastCellNum(); colnum++) {
								try {
									rowData.add(row.getCell(colnum).getStringCellValue());
								} catch (NullPointerException npe) {
									rowData.add("");
								}
								catch(IllegalStateException lse) {
									rowData.add(Double.valueOf(row.getCell(colnum).getNumericCellValue()).toString());
								}
							}
							((DefaultTableModel) tables.get(tableName).getModel()).addRow(rowData);
						}

						for (int colnum = topRow.getFirstCellNum(); colnum < topRow.getLastCellNum(); colnum++) {
							if (!visibleHeadings.contains(headings.get(colnum))) {
								tables.get(tableName).removeColumn(tables.get(tableName).getColumn(tables.get(tableName)
									.getColumnName(tables.get(tableName).convertColumnIndexToView(colnum))));
								
							}
						}
					}
				}
				headings.clear();
			}
			try {
				workbook.close();
				fis.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
