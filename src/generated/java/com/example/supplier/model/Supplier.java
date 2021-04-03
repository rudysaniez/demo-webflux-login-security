package com.example.supplier.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Supplier {

	@Exclude
	private String id;
	
	private Integer supplierId;
	
	@Exclude
	private String description;
}
