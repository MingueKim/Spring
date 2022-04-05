#Section 3

#
    새로운 할인 정책과 개발
    -> 객체지향적으로 설계가 되었는가?

***할인 정책이 바뀐다는 전제 하에, 새롭게 코드를 변경해본다.***

#
    새로운 할인 정책 적용과 문제점
    - 역할과 구현을 충실하게 분리했다. -> O
    - 다형성 활용, 인터페이스와 구현 객체를 분리했다. -> O
    - OCP, DIP 같은 객체지향 설계 원칙을 철저히 준수했다. -> X

주문 서비스 클라이언트(OrderServiceImple)의 클래스 의존 관계를 분석해보면, 
추상뿐만 아니라 구체 클래스에도 의존하고있다.
추상 의존: DiscountPolicy(인터페이스)
구현 클래스: FixDiscountPolicy(클래스), RateDiscountPolicy(클래스)
--> DIP 위반!

지금의 코드는 기능을 확장해서 변경하면, 클라이언트 코드에 영향을 준다. 
--> OCP 위반!

#
    어떻게 문제를 해결할 수 있을까?

- 클라이언트 코드인 OrderServiceImpl 은 DiscountPolicy의 인터페이스 뿐만 아니라 구체 클래스도 함께 의존한다.
- 그래서 구체 클래스를 변경할 때 클라이언트 코드도 함께 변경해야 한다.
- DIP위반 0> 추상에만 의존하도록 변경
- DIP를 위반하지 않도록 인터페이스에만 의존할 수 있게 의존관계를 변경한다. 

#
    이전 코드
    private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
    private final DiscountPolicy discountPolicy = new RateDiscountPolicy();

    수정 코드
    private DiscountPolicy discountPolicy

어떻게 NullPointerException 을 없앨 수 있을까?
--> 누군가가 클라이언트인 OrderServiceImpl 에 DiscountPolicy 의 구현 객체를 대신 생성하고 주입해주어야 한다. 


#
    관심사의 분리

- 애플리케이션을 하나의 공연이라 생각해볼 때, 각각의 인터페이스가 배역이라면, 배역에 맞는 배우를 선택하는 것은 누가 하는가?
- 로미오와 줄리엣 역할은 배우가 정하는 것이 아니다. 이전 코드는 마치 로미오 역할(인터페이스)인 디카프리오(구현체) 줄리엣 역할(인터페이스)을 하는 여자 주인공(구현체)를 직접 초빙하는 것과 다름 없다.
- OrderServiceImple(클래스, 구현체)은 OrderService에 관련된 로직만 수행해야 하는데, 자신이 discountPolicy(인터페이스)를 직접 생성하고 선택(Fix, Rate)했다.

***관심사를 분리하자***
- 배우는 본인의 역할인 배역을 수행하는 것에만 집중해야 한다.
- 디카프리오는 어떤 여자 주인공이 선택되더라도 똑같이 공연을 할 수 있어야 한다.
- 공연을 구성하고, 담당 배우는 섭외하고, 역할게 맞는 배우를 지정하는 책임을 담당하는 별도의 공연 기획자가 나올 시점이다.
- 공연 기획자를 만들고, 배우와 공연 기획자의 책임을 확실히 분리하자.

#
    AppConfig의 등장

AppConfig는 애플리케이션의 실제 동작에 필요한 구현 객체를 생성
- MemberServiceImpl
- MemoryMemberRepository
- FixDiscountPolicy

AppConfig는 생성한 객체 인스턴스의 참조(레퍼런스)를 생성자를 토앻서 주입해준다.
- MemberServiceImpl -> MemoryMemberRepository
- OrderServiceImpl -> MemoryMemberRepository, FixDiscountPolicy

#
    // 생성자 주입
    바뀐 OrderServiceImpl
    public MemberService memberService() {
    return new MemberServiceImpl(new MemoryMemberRepository());
    }

    public OrderService orderService() {
        return new OrderServiceImpl(new MemoryMemberRepository(), new FixDiscountPolicy());
    }

- 설계 변경으로 MemberServiceImpl 은 MemoryMemberRepository 를 의존하지 않는다 !
- 단지 MemberRepository 인터페이스만 의존한다.
- MemberServiceImpl 입장에서 생성자를 통해 어떤 구현 객체가 들어올 지 (주입될 지) 알 수 없다.
- MemberServiceImpl 은 이제부터 의존관계에 대한 고민은 외부에 맡기고 실행에만 집중하면 된다.

#
    바뀐 MemberServiceImpl
    private final MemberRepository memberRepository; // 인터페이스

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    

Appconfig 객체는 memoryMemberRepository 객체를 생성하고 

그 참조값을 memberServiceImpl 을 생성하면서 생성자로 전달한다.

클라이언트인 memberServiceImpl 입장에서 보면 의존관계를 마치 외부에서 주입해주는 것 같다고 해서 

DI(Dependency Injection) 우리 말로 의존관계 주입 또는 의존성 주입이라 한다. 

- 설계 변경으로 OrderServiceImpl은 FixDiscountPolicy를 의존하지 않는다.
- 단지 DiscountPolicy 인터페이스만 의존한다.
- OrderServiceImpl 입장에서 생성자를 통해 어떤 구현 객체가 들어올 지 (주입될 지) 알 수 없다.
- OrderServiceImpl 은 이제부터 의존관계에 대한 고민은 외부에 맡기고 실행에만 집중하면 된다.

#
    public MemberService memberService() {

        return new MemberServiceImpl(memberRepository());
    }

    private MemoryMemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    public OrderService orderService() {
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }

    public DiscountPolicy discountPolicy() {
        return new FixDiscountPolicy();
    }

new MemoryMemberRepository 이 부분이 중복 제거되었다.

이제 MemoryMemberRepository를 다른 구현체로 변경할 때 한 부분만 변경하면 된다.

AppConfig 를 보면 역할과 구현 클래스가 한 눈에 들어온다.

애플리케이션 전체 구성이 어떻게 되어있는지 빠르게 파악할 수 있다.

#
    새로운 구조와 할인 정책 적용

AppConfig 에서 FixDiscountPolicy 에서 RateDiscountPolicy 로 변경.

이제 할인 정책을 변경해도 애플리케이션의 구성 역할을 담당하는 AppConfig 만 변경하면 된다.

클라이언트 코드인 OrderServiceImpl 을 포함해서 사용 영역의 어떤 코드도 변경할 필요가 없다.

구성 영역은 당연히 변경된다. 구성 역할을 담당하는 AppConfig 를 애플리케이션이라는 공연의 기획자로 생각하자.

공연 기획자는 공연 참여자인 구현 객체들을 모두 알아야 한다.


#

###### 이번 수업을 듣고, 자바의 인터페이스, 클래스(추상화와 구현체)에 대한 이해가 조금 부족한 것 같다는 생각이 들었다. 
###### 자바로 개발을 본격적으로 공부하기 이전에 먼저 기본적인 자바의 이해도를 높일 필요가 있을 것 같다.
###### 핵심 공부 단어: 인터페이스, 클래스, 클라이언트, this, final, 생성자
