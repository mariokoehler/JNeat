package de.mkoehler.neat.util;

import java.util.ArrayList;
import java.util.List;

public class LootTable {

    private static final String TEMPLATE = "0x %-40s| %-39s%9s| %-20s%11s";

    private static final List<List<String>> items = new ArrayList<>();

    public static void main(String[] args) {

        items.add( List.of("Sigil of Ergamen",               "Hairband of Quiet Gaze",      "hat/L",  "Pacify") );
        items.add( List.of("Saint Veridiana’s Breath",               "Clerics Cap of Absolution",      "hat/L",  "Purify Self") );
        items.add( List.of("Echo of Moon",                   "Magus' Hat of Fractured Time","hat/L",  "Chronoseer") );
        items.add( List.of("Rosarium of Silent Intercession","Cleric's Alb of Reflection"  ,"chest/L","Divine Shield") );
        items.add( List.of("Reliquary of Choir","Shepherd's Smock of Blooming Fold","chest/L","Verdant Mend") );
        items.add( List.of("Amulet of Martyr’s Crown","Magus' Robe of Concentration","chest/L","Alacrity") );
        items.add( List.of("Amulet of Justice","Pilgrim's Tunic","chest/L","Blessed Breath") );
        items.add( List.of("Seal of the Lost Abbot","Cleric's Alb of Cleansing","chest/L","Purify Target") );
        items.add( List.of("Amulet of Holy Wound","Magus' Trousers of Entrancement","pants/L","Mesmerize") );
        items.add( List.of("Amulet of Veil","Magus' Trousers of Dreambind","pants/L","Haunting Memories") );
        items.add( List.of("Seal of the Flame","Cleric's Pantlet of the Hollow Seal","pants/L","Consecrated Ground") );
        items.add( List.of("Amulet of the Seventh Mystery","Shepherd's Steps of Rushing Wind","shoes/L","Spirit Stride") );

        items.add( List.of("Sigil of Tachan","Hunter's Hood of the Marked Prey","hat/M","Killmark") );
        items.add( List.of("Sigil of Ambolon","Trickster's Coif of the Slosh","hat/M","Jesters Flask") );
        items.add( List.of("Sigil of Carelena","Vanguard's Cap of Revocation","hat/M","Revoke") );
        items.add( List.of("Rosary of the 99 Pains","Vanguard's Surcoat of Divine Fervor","chest/M","Frenzy") );
        items.add( List.of("Seal of Creed","Trickster's Doublet of the Whiplash","chest/M","Chrono Ward") );
        items.add( List.of("Talisman of the Nine Litanies","Pilgrim's Jerkin","chest/M","Blessed Breath") );
        items.add( List.of("Seal of the Martyrs","Vanguard's Breeches of Holy Strikes","pants/M","Sacrament of Strife") );
        items.add( List.of("Sigil of Pellipis","Hunter's Trousers of Binding Roots","pants/M","Netfall") );
        items.add( List.of("Charm of the Immaculate Grief","Pilgrim's Marchers","shoes/M","Breath of Joy II") );

        items.add( List.of("Blessed Ligarius","Herald's Helm of the Immovable","hat/H","Mental Fortitude") );
        items.add( List.of("Talisman of Litanies","Herald's Plates of the Ironroot ","chest/H","Ironroot Grasp") );
        items.add( List.of("Sigil of Magalast","Ironside's Harnais of Provocation","chest/H","Taunt") );
        items.add( List.of("Sigil of Alcanor","Warrior's Cuirass of Windlash","chest/H","Windlash") );
        items.add( List.of("Rosarium of Equilibrium","Pilgrim's Breastplate","chest/H","Blessed Breath") );
        items.add( List.of("Sigil of Akium","Herald's Greaves of the Unfallen","pants/H","Rallying Banner") );
        items.add( List.of("Amulet of Sanctity","Ironside's Greaves of Sacred Guard","pants/H","Sanctified Stand") );
        items.add( List.of("Amulet of Holy See","Ironside's Greaves of Vital Pulse","pants/H","Aura of Life") );
        items.add( List.of("Icon of the Choir","Warrior's Sabatons of Swift March","shoes/H","Surging Stride") );
        items.add( List.of("Sigil of Lamolon","Ironside's Sabatons of Quiet Guard","shoes/H","Quiet Stand") );
        items.add( List.of("Seal of the Lost Abbot","Pilgrim's Sabatons","shoes/H","Breath of Joy II") );

        for( var i : items ) {
            System.out.println( String.format(TEMPLATE, i.get(0), i.get(1)+" II", "("+i.get(2)+")", i.get(3), "(I/Lv.10)"));
            System.out.println( String.format(TEMPLATE,"Great "+i.get(0),i.get(1)+" III","("+i.get(2)+")",i.get(3),"(II/Lv.20)"));
            System.out.println( String.format(TEMPLATE,"Majestic "+i.get(0),i.get(1)+" IV","("+i.get(2)+")",i.get(3),"(III/Lv.30)"));
        }
    }
}
