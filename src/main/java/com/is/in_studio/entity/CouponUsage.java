package com.is.in_studio.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "coupon_usage", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"coupon_id", "user_id"})
})
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_usage_id")
    private Integer couponUsageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "used_at", nullable = false, updatable = false)
    private OffsetDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        this.usedAt = OffsetDateTime.now();
    }

    public Integer getCouponUsageId() { return couponUsageId; }
    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public OffsetDateTime getUsedAt() { return usedAt; }
}
