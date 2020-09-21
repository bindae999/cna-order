package com.example.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import javax.persistence.*;

@Entity
@Table(name = "ORDER_TABLE")
public class Order {

    @Id @GeneratedValue
    Long id;
    Long productId;
    String productName;
    int qty;

    //저장후에 호출하게 하는 Annotation
    @PostPersist
    public void onPostPersist(){
        //이벤트 발행
        OrderPlaced orderPlaced = new OrderPlaced();
        orderPlaced.setProductName(this.getProductName());
        orderPlaced.setProductId(this.getProductId());
        orderPlaced.setQty(this.getQty());

        //해당클래스를 json으로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String json  = null;

        try {
            json = objectMapper.writeValueAsString(orderPlaced);
        }catch (JsonProcessingException e){
            throw new RuntimeException("JSON format excetption", e);
        }
        //System.out.println(json);

        //메시지큐에 Publish
        Processor processor = OrderApplication.applicationContext.getBean(Processor.class);
        MessageChannel outputChannel = processor.output();

        outputChannel.send(MessageBuilder
                .withPayload(json)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build());


    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }
}
