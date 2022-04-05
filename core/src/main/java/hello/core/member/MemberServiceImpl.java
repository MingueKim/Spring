package hello.core.member;

public class MemberServiceImpl implements MemberService{

    // MemberServiceImpl 은 MemberRepository(인터페이스=추상화), MemoryMemberRepository(구현체)에도 의존한다.
    // DIP 위반.`
    private final MemberRepository memberRepository; // 인터페이스

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public void join(Member member) {
        memberRepository.save(member);
    }

    @Override
    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId);
    }
}
