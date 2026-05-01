1. HttpServletRequest, HttpServletReponse 는 서비스 레이어로 내려가는건 안좋은 패턴이다.
별도로 따로 *Resolver 클래스를 만들어서 거기서 관리하는게 좋다.