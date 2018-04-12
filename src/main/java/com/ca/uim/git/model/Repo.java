package com.ca.uim.git.model;

import java.util.List;

public class Repo {

	String repoName;
	
	private List<Branch> branchs;
	
	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public List<Branch> getBranchs() {
		return branchs;
	}

	public void setBranchs(List<Branch> branchs) {
		this.branchs = branchs;
	}


}
