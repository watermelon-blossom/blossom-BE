package com.watermelon.dateapp.global.common;

import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class BaseEntity {
	@Comment("생성 일시")
	@Column(name = "createDate", nullable = false, updatable = false)
	@CreatedDate
	protected LocalDateTime createdDate;

	@Comment("수정 일시")
	@Column(name = "modifiedDate")
	protected LocalDateTime modifiedDate;

	@Comment("생성자 아이디")
	@Column(name = "createdBy", nullable = false, updatable = false)
	@CreatedBy
	protected String createdBy;

	@Comment("수정자 아이디")
	@Column(name = "modifiedBy", length = 50)
	protected String modifiedBy;
}
