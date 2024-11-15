package psk.ponb;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@EqualsAndHashCode
@Data
public class Post {
    private String id;
    private String user;
    private String avatar;
    private String title;
    private LocalDateTime createdAt;
}
