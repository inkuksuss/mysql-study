package com.example.fastcampusmysql.domain.member;

import com.example.fastcampusmysql.domain.util.MemberFixtureFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MemberTest {

    @Test
    @DisplayName("회원 이름 변경")
    public void testChangeName() {
        var member = MemberFixtureFactory.create();
        var expected = "change";
        member.changeNickname(expected);

        Assertions.assertEquals(expected, member.getNickname());
    }

    @Test
    @DisplayName("회원 이름 길이")
    public void testNickNameMaxLength() {
        var member = MemberFixtureFactory.create();
        var overMaxLength = "changeeeeeeeeee";

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> member.changeNickname(overMaxLength));
    }
}
