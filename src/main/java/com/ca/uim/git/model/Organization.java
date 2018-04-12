package com.ca.uim.git.model;

import java.util.List;

public class Organization {

	String organizationName;

	private List<Repo> repos;
	
	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public List<Repo> getRepos() {
		return repos;
	}

	public void setRepos(List<Repo> repos) {
		this.repos = repos;
	}
}
