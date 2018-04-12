package com.ca.uim.git.datastore;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ca.uim.git.model.Branch;
import com.ca.uim.git.model.Organization;
import com.ca.uim.git.model.Repo;

public class BranchStore {

	XSSFWorkbook workbook = new XSSFWorkbook();
	XSSFSheet sheet = workbook.createSheet("Stale Branches");
	int rownum = 0;
	private String fileName = "";
	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	DateFormat outputFormatter = new SimpleDateFormat("dd/mm/yyyy");

	public void storeData(Organization org) throws ParseException {
		String currentUsersHomeDir = System.getProperty("user.home");
		fileName = currentUsersHomeDir + "/" + org.getOrganizationName() + "_StaleBranches.xlsx";
		System.out.println("Creating Excel Data");
		Row header = sheet.createRow(rownum++);
		header.createCell(0).setCellValue("Repository Name");
		header.createCell(1).setCellValue("Branch Name");
		header.createCell(2).setCellValue("Last Commited Author");
		header.createCell(3).setCellValue("Last Commited Date");
		header.createCell(4).setCellValue("Uncommited Days");
		for (Repo repo : org.getRepos()) {
			for (Branch branch : repo.getBranchs()) {
				Row row = sheet.createRow(rownum++);
				int colNum = 0;
				Cell cell1 = row.createCell(colNum++);
				cellType(cell1, repo.getRepoName());
				Cell cell2 = row.createCell(colNum++);
				cellType(cell2, branch.getBranchName());
				Cell cell3 = row.createCell(colNum++);
				cellType(cell3, branch.getLastCommitedAuthor());
				Cell cell4 = row.createCell(colNum++);
				cellType(cell4, branch.getLastCommitedDate());
				Cell cell5 = row.createCell(colNum++);
				cellType(cell5, branch.getNoCommitDays());
			}
		}
		try {
			FileOutputStream outputStream = new FileOutputStream(fileName);
			workbook.write(outputStream);
			workbook.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done");
	}

	private void cellType(Cell cell, Object input) {
		if (input instanceof String) {
			cell.setCellValue((String) input);
		} else if (input instanceof Long) {
			cell.setCellValue((Long) input);
		}
	}
}