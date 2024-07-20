package life.ggumtle.token.common.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "survey")
public class Survey {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("environment")
    private Boolean environment = false;

    @Column("charity")
    private Boolean charity = false;

    @Column("relationships")
    private Boolean relationships = false;

    @Column("relaxation")
    private Boolean relaxation = false;

    @Column("romance")
    private Boolean romance = false;

    @Column("exercise")
    private Boolean exercise = false;

    @Column("travel")
    private Boolean travel = false;

    @Column("lang")
    private Boolean lang = false;

    @Column("culture")
    private Boolean culture = false;

    @Column("challenge")
    private Boolean challenge = false;

    @Column("hobby")
    private Boolean hobby = false;

    @Column("workplace")
    private Boolean workplace = false;
}
