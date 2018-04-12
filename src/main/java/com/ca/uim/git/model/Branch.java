package com.ca.uim.git.model;

public class Branch {

	String branchName;

	String lastCommitedDate;

	String lastCommitedAuthor;
	
	private long noCommitDays;

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getLastCommitedDate() {
		return lastCommitedDate;
	}

	public void setLastCommitedDate(String lastCommitedDate) {
		this.lastCommitedDate = lastCommitedDate;
	}

	public String getLastCommitedAuthor() {
		return lastCommitedAuthor;
	}

	public void setLastCommitedAuthor(String lastCommitedAuthor) {
		this.lastCommitedAuthor = lastCommitedAuthor;
	}

	public long getNoCommitDays() {
		return noCommitDays;
	}

	public void setNoCommitDays(long noCommitDays) {
		this.noCommitDays = noCommitDays;
	}

}
