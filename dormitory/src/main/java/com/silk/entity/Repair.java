package com.silk.entity;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import com.silk.utils.Entity;
import java.util.Date;
import java.util.List;


/**
 * @author LindaSilk
 * @date 2021年3月13日, 周六
 */
public class Repair extends Entity{

	private Integer id;

	private String repItem;

	private String description;

	private Date repDate;

	private Integer stuId;

	private Integer buildingId;

	/**
	 * @silkTag 非数据库字段，仅作为查询条件使用
	 * @description 作用：一个宿管员可管理多栋楼，以List存储其管理的所有楼栋ID，用于查询这些楼栋的维修申请
	 */
	private List<Integer> buildingIds;

	private Integer roomId;

	private Integer repStatus;		// 维修状态：等待维修=0；维修完成(学生已确认)=1；虚假报修=2；待学生确认=3

	private String repMan;


	/**
	 * 引入User、Building、Room三个实体
	 */
	private User user;
	private Building building;
	private Room room;



	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	public String getRepItem() {
		return repItem;
	}

	public void setRepItem(String repItem) {
		this.repItem = repItem;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getRepDate() {
		return repDate;
	}

	public void setRepDate(Date repDate) {
		this.repDate = repDate;
	}

	public Integer getStuId() {
		return stuId;
	}

	public void setStuId(Integer stuId) {
		this.stuId = stuId;
	}

	public Integer getBuildingId() {
		return buildingId;
	}

	public void setBuildingId(Integer buildingId) {
		this.buildingId = buildingId;
	}

	public List<Integer> getBuildingIds() {
		return buildingIds;
	}

	public void setBuildingIds(List<Integer> buildingIds) {
		this.buildingIds = buildingIds;
	}

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public Integer getRepStatus() {
		return repStatus;
	}

	public void setRepStatus(Integer repStatus) {
		this.repStatus = repStatus;
	}

	public String getRepMan() {
		return repMan;
	}

	public void setRepMan(String repMan) {
		this.repMan = repMan;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Building getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		this.building = building;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}
}