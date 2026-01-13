package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
// uniqueConstraints: 복합 유니크 제약 조건, (repository_id, branch_name) 조합이 유일해야 함
// indexes: repository_id에 인덱스를 생성하여 조회 성능 향상
// SELECT * FROM git_branches WHERE repository_id = ? 이런 쿼리를 많이 사용하므로 index 생성
@Table(
        name = "git_branches",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_git_branches_repo_name",
                        columnNames = {"repository_id", "branch_name"}
                )
        },
        indexes = {
                @Index(name = "idx_git_branches_repo", columnList = "repository_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BranchEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 생성을 DB에게 완전히 위임
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)    // 여러 브랜치 -> 하나의 레포지토리, 연관 엔티티를 필요할 때만 조회(즉시 join x)
    @JoinColumn(name = "repository_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_branches_repo"))    // 이 엔티티의 테이블에 생성될 때 repository_id 컬럼이 생성됨. 이 컬럼이 연관된 엔티티의 pk를 참조함
    private ProjectRepositoryEntity repository;

    @Column(name = "branch_name", nullable = false, length = 300)
    private String branchName;

    // PR merge 여부를 branch 수준에서 캐시하고 싶으면(선택)
    @Column(nullable = false)
    private boolean merged;
}