package com.express.service;

import com.express.dto.MemberRequest;
import com.express.entity.Member;
import com.express.repository.MemberRepository;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member createMember(MemberRequest request) {
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("手机号已存在: " + request.getPhone());
        }

        Member member = new Member();
        member.setName(request.getName());
        member.setPhone(request.getPhone());
        member.setAddress(request.getAddress());
        member.setDiscount(request.getDiscount());

        return memberRepository.save(member);
    }

    @Transactional
    public Member updateMember(Long id, MemberRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("会员不存在: " + id));

        if (request.getName() != null) {
            member.setName(request.getName());
        }
        if (request.getPhone() != null) {
            memberRepository.findByPhone(request.getPhone()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("手机号已被其他会员使用: " + request.getPhone());
                }
            });
            member.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            member.setAddress(request.getAddress());
        }
        if (request.getDiscount() != null) {
            member.setDiscount(request.getDiscount());
        }

        return memberRepository.save(member);
    }

    @Transactional
    public void deleteMember(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new EntityNotFoundException("会员不存在: " + id);
        }
        memberRepository.deleteById(id);
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("会员不存在: " + id));
    }

    public Member getMemberByPhone(String phone) {
        return memberRepository.findByPhone(phone)
                .orElseThrow(() -> new EntityNotFoundException("会员不存在，手机号: " + phone));
    }

    public List<Member> searchMembers(String name) {
        if (name != null && !name.trim().isEmpty()) {
            return memberRepository.findByNameContaining(name);
        }
        return memberRepository.findAll();
    }
}
