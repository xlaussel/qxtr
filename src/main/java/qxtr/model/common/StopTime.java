package qxtr.model.common;

import lombok.EqualsAndHashCode;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;


@EqualsAndHashCode
public class StopTime {

    public StopTime(int hour,int minute, int second) {
        this.hour = (byte) hour;
        this.minute = (byte) minute;
        this.second = (byte) second;
    }

    public StopTime(int seconds) {
        this.hour=(byte)(seconds/3600);
        seconds%=3600;
        this.minute=(byte)(seconds/60);
        this.second=(byte)(seconds%60);
    }

    public static StopTime parse(String text) {
        var vals=text.split(":");
        return new StopTime(Integer.parseInt(vals[0]),Integer.parseInt(vals[1]),Integer.parseInt(vals[2]));
    }

    public String toString() {
        return String.format("%2d:%2d:%2d",hour,minute,second);
    }

    public int toSeconds() {
        return 3600*hour+60*minute+second;
    }

    private final byte hour;
    private final byte minute;
    private final byte second;

    @Converter
    public static class converter implements AttributeConverter<StopTime,Integer> {

        @Override
        public Integer convertToDatabaseColumn(StopTime stopTime) {
            return stopTime.toSeconds();
        }

        @Override
        public StopTime convertToEntityAttribute(Integer dbData) {
            return new StopTime(dbData);
        }
    }


}
