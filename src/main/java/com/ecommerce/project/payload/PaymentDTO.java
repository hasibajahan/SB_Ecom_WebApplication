package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
	private Long paymentId;//payment id in the application
	private String paymentMethod;
	private String pgPaymentId;//Payment id/reference id given by the particular payment gateway
	private String pgStatus;
	private String pgResponseMessage;
	private String pgName;
}
