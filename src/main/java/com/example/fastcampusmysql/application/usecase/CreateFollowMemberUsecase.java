package com.example.fastcampusmysql.application.usecase;

import com.example.fastcampusmysql.domain.follow.service.FollowWriteService;
import com.example.fastcampusmysql.domain.member.service.MemberReadService;
import org.springframework.stereotype.Service;

@Service
public class CreateFollowMemberUsecase {

    private final MemberReadService memberReadService;
    private final FollowWriteService followWriteService;

    public CreateFollowMemberUsecase(MemberReadService memberReadService, FollowWriteService followWriteService) {
        this.memberReadService = memberReadService;
        this.followWriteService = followWriteService;
    }

    public void execute(Long fromMemberId, Long toMemberId) {
        var fromMember = memberReadService.getMember(fromMemberId);
        var toMember= memberReadService.getMember(toMemberId);
        followWriteService.create(fromMember, toMember);
    }
}
