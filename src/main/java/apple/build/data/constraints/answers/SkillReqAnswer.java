package apple.build.data.constraints.answers;

public class SkillReqAnswer {
    public final boolean valid;
    public final int[] mySkills;
    public final int extraSkillPoints;


    public SkillReqAnswer(boolean valid, int[] mySkills, int extraSkillPoints) {
        this.valid = valid;
        this.mySkills = mySkills;
        this.extraSkillPoints = extraSkillPoints;
    }
}
