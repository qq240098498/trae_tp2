package com.express.controller;

import com.express.dto.MemberRequest;
import com.express.entity.Member;
import com.express.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<Member> createMember(@RequestBody MemberRequest request) {
        Member member = memberService.createMember(request);
        return new ResponseEntity<>(member, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers(@RequestParam(required = false) String name) {
        List<Member> members = memberService.searchMembers(name);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        Member member = memberService.getMemberById(id);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<Member> getMemberByPhone(@PathVariable String phone) {
        Member member = memberService.getMemberByPhone(phone);
        return ResponseEntity.ok(member);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody MemberRequest request) {
        Member member = memberService.updateMember(id, request);
        return ResponseEntity.ok(member);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}
