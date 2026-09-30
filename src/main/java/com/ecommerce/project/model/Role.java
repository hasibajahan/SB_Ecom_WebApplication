package com.ecommerce.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name="roles")//to specify the table name
public class Role {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="role_id")
	private Integer roleId;
	
	@ToString.Exclude
	@Enumerated(EnumType.STRING)//And by default if you are persisting enum type into database, it is persisted as an integer. So if we wanna persist as a string we have to write this.
	@Column(length=20,name="role_name")
	private AppRole roleName;

	public Role(AppRole roleName) {
		this.roleName = roleName;
	}
}
