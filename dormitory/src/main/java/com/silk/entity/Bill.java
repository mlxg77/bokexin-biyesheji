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
public class Bill extends Entity{


	private Integer id;
	private Integer billYear;
	private Integer billMonth;
	private Integer buildingId;
	private Integer roomId;

	private Double waterUsed;
	private Double waterFee;
	private Double energyUsed;
	private Double energyFee;
	private Double totalFee;
	/**
	 * 账单状态：未生成=0（学生已生成账单，等待宿管员推送）；已推送=1（待学生支付）；已支付=2
	 */
	private Integer paid;


	private Room room;
	private Building building;
	private User user;

	/**
	 * @description 作用：一个宿管员可管理多栋楼，以List存储其管理的所有楼栋ID，用于查询这些楼栋的账单
	 */
	private List<Integer> buildingIds;




	// Constructor


	public Bill() {
	}

	public Bill(Integer id, Integer billYear, Integer billMonth, Integer buildingId, Integer roomId, Double waterUsed, Double waterFee, Double energyUsed, Double energyFee, Double totalFee, Integer paid) {
		this.id = id;
		this.billYear = billYear;
		this.billMonth = billMonth;
		this.buildingId = buildingId;
		this.roomId = roomId;
		this.waterUsed = waterUsed;
		this.waterFee = waterFee;
		this.energyUsed = energyUsed;
		this.energyFee = energyFee;
		this.totalFee = totalFee;
		this.paid = paid;
	}

	// getter and setter
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getBillYear() {
		return billYear;
	}

	public void setBillYear(Integer billYear) {
		this.billYear = billYear;
	}

	public Integer getBillMonth() {
		return billMonth;
	}

	public void setBillMonth(Integer billMonth) {
		this.billMonth = billMonth;
	}

	public Integer getBuildingId() {
		return buildingId;
	}

	public void setBuildingId(Integer buildingId) {
		this.buildingId = buildingId;
	}

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public Double getWaterUsed() {
		return waterUsed;
	}

	public void setWaterUsed(Double waterUsed) {
		this.waterUsed = waterUsed;
	}

	public Double getWaterFee() {
		return waterFee;
	}

	public void setWaterFee(Double waterFee) {
		this.waterFee = waterFee;
	}

	public Double getEnergyUsed() {
		return energyUsed;
	}

	public void setEnergyUsed(Double energyUsed) {
		this.energyUsed = energyUsed;
	}

	public Double getEnergyFee() {
		return energyFee;
	}

	public void setEnergyFee(Double energyFee) {
		this.energyFee = energyFee;
	}

	public Double getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(Double totalFee) {
		this.totalFee = totalFee;
	}

	public Integer getPaid() {
		return paid;
	}

	public void setPaid(Integer paid) {
		this.paid = paid;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public Building getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		this.building = building;
	}

	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public List<Integer> getBuildingIds() {
		return buildingIds;
	}
	
	public void setBuildingIds(List<Integer> buildingIds) {
		this.buildingIds = buildingIds;
	}
	


}