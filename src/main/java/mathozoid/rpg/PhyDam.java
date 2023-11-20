package mathozoid.rpg;

public record PhyDam(float blunt,float cutting,float piercing){

    private PhyDam(float allValues){
        this(allValues,allValues,allValues);
    }

    public PhyDam mul(float factor){
        return mul(new PhyDam(factor));
    }
    public PhyDam mul(PhyDam dam){
        return new PhyDam(
                this.blunt*dam.blunt,
                this.cutting*dam.cutting,
                this.piercing*dam.piercing
        );
    }
    public PhyDam inverse(PhyDam original){
        return new PhyDam(
                1/original.blunt,
                1/original.cutting,
                1/original.piercing
        );
    }

    public PhyDam add(float additive){
        return add(new PhyDam(additive));
    }
    public PhyDam add(PhyDam dam){//WARNING: DOES NOT ALLOW NEGATIVE SUMS, SEE NOTES
        float blunt=(this.blunt+dam.blunt);
        float cutting=(this.cutting+dam.cutting);
        float piercing=(this.piercing+dam.piercing);
        return new PhyDam(
                blunt<0?0:blunt,
                cutting<0?0:cutting,
                piercing<0?0:piercing
        );
    }
    public PhyDam reverse(PhyDam original){
        return new PhyDam(
                -original.blunt,
                -original.cutting,
                -original.piercing
        );
    }

}
