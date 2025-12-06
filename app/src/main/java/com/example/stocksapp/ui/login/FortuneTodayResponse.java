package com.example.stocksapp.ui.login;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FortuneTodayResponse {

    @SerializedName("name")
    private String name;

    @SerializedName("birthdate")
    private String birthdate;

    @SerializedName("sign")
    private String sign;

    @SerializedName("fortune")
    private Fortune fortune;

    public String getName() {
        return name;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public String getSign() {
        return sign;
    }

    public Fortune getFortune() {
        return fortune;
    }

    public static class Fortune {

        @SerializedName("overall")
        private String overall;

        @SerializedName("money")
        private String money;

        @SerializedName("love")
        private String love;

        @SerializedName("work_study")
        private String workStudy;

        @SerializedName("health")
        private String health;

        @SerializedName("lucky_item")
        private String luckyItem;

        @SerializedName("lucky_color")
        private String luckyColor;

        @SerializedName("summary_keywords")
        private List<String> summaryKeywords;

        public String getOverall() {
            return overall;
        }

        public String getMoney() {
            return money;
        }

        public String getLove() {
            return love;
        }

        public String getWorkStudy() {
            return workStudy;
        }

        public String getHealth() {
            return health;
        }

        public String getLuckyItem() {
            return luckyItem;
        }

        public String getLuckyColor() {
            return luckyColor;
        }

        public List<String> getSummaryKeywords() {
            return summaryKeywords;
        }
    }
}
