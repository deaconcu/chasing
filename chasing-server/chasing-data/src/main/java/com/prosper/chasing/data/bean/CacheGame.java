package com.prosper.chasing.data.bean;

import java.util.Set;

public class CacheGame {

    private String id;
    private Integer metagameId;
    private int duration;
    private int creatorId;
    private String createTime;
    private Set<String> users;
    
    public CacheGame() {
    	
    }
    
    public CacheGame(String s) {
		String[] parts = s.split("_");
		if (parts.length < 4) {
			throw new RuntimeException("parse error");
		}
		
		metagameId = Integer.parseInt(parts[0]);
		duration = Integer.parseInt(parts[1]);
		creatorId = Integer.parseInt(parts[2]);
		createTime = parts[3];
	}
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getMetagameId() {
		return metagameId;
	}
	public void setMetagameId(Integer metagameId) {
		this.metagameId = metagameId;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public int getCreatorId() {
		return creatorId;
	}
	public void setCreatorId(int creatorId) {
		this.creatorId = creatorId;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	
	public String toString() {
		return metagameId.toString() + "_" + duration + "_" + Integer.toString(creatorId) + "_" + createTime; 
	}

	public Set<String> getUsers() {
		return users;
	}

	public void setUsers(Set<String> users) {
		this.users = users;
	}
}
