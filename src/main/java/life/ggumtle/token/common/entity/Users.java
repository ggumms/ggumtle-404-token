package life.ggumtle.token.common.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@Table("users")
public class Users implements UserDetails {

    @Id
    private Long id;

    @Column("provider_id")
    private String providerId;

    @Column("provider")
    private Provider provider;

    @Column("internal_id")
    private String internalId;

    @Column("nickname")
    private String nickname;

    @Column("profile_image")
    private String profileImage;

    @Column("account_status")
    private Boolean hasAccount = false;

    @Column("survey_status")
    private Boolean surveyStatus = false;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column("is_deleted")
    private boolean isDeleted = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return nickname;
    }
}