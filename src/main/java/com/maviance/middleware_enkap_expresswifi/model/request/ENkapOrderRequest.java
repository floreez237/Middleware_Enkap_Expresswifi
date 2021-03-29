package com.maviance.middleware_enkap_expresswifi.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.List;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class ENkapOrderRequest {
    private String currency;
    private String merchantReference;//which is to represent the paymentID
    private String phoneNumber;
    private String receiptUrl;//which represents the callback URL
    private String orderDate;
    private String description;
    @JsonProperty("id")
    private CustomId customId;
    @JsonProperty("items")
    private List<Item> itemList;
    private Double totalAmount;

    public void calculateTotalAmount() {
        totalAmount = itemList.stream().mapToDouble(Item::getSubTotal).sum();
    }

    public void addItem(Item item) {
        itemList.add(item);
    }

    @Data
    @With
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        private String itemId;
        private String particulars;
        private int quantity;
        private double unitCost;
        private double subTotal;

        public void calculateSubtotal() {
            subTotal = quantity * unitCost;
        }
    }

    @Data
    @With
    @AllArgsConstructor
    public static class CustomId {
        private String uuid;
        private String version;

    }

}

@Data
@With
@AllArgsConstructor
class Item {
    private String itemId;
    private String particulars;
    private int quantity;
    private double unitCost;
    private double subTotal;

    public void calculateSubtotal() {
        subTotal = quantity * unitCost;
    }
}

@Data
@With
@AllArgsConstructor
class CustomId {
    private String uuid;
    private String version;

}
