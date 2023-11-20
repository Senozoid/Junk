package mathozoid.rpg;

public record MagDam(MagDamType type, float potence, int range){
    public enum MagDamType{
        COLD,
        HEAT,
        ELECTRIC,
        CORROSIVE,
        PSYCHIC,
        FORCE,
        ELDRITCH;
    }
}
