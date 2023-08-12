package com.process.DTO;

public class TaskBean {

    private String assignee;

    private String name;

    private  String id;

    private String tenanted;

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenanted() {
        return tenanted;
    }

    public void setTenanted(String tenanted) {
        this.tenanted = tenanted;
    }
}
