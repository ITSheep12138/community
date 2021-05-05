package com.community.bean;
import java.io.Serializable;
import java.util.Date;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscussPost implements Serializable {

  private int id;
  private int userId;
  private String title;
  private String content;
  private int type;
  private int status;
  private Date createTime;
  private int commentCount;
  private double score;

}
