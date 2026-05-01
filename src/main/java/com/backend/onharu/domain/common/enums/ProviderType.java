package com.backend.onharu.domain.common.enums;

public enum ProviderType { 
    LOCAL,
    KAKAO,
    ;
    
    /**
     * OAuth2 제공자 이름으로 ProviderType 변환
     * @param provider 제공자 이름 (kakao)
     * @return ProviderType   
     */
    public static ProviderType fromOAuth2Provider(String provider) {
        if (provider == null) {
            return LOCAL;
        }
        return switch (provider.toLowerCase()) {
            case "kakao" -> KAKAO;
            default -> LOCAL;
        };
    }
}
