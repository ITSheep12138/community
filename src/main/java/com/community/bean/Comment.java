package com.community.bean;
import java.util.Date;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

  private int id;
  private int userId;
  private int entityType;
  private int entityId;
  private int targetId;
  private String content;
  private int status;
  private Date createTime;

}
