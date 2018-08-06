package org.rogerunwin.devices.base;

public abstract class Actuator {
    // servoId is type int to avoid having to cast constantly, this may be
    // reconsidered down the road.
    public Boolean setPosition(int servoId, short pos, short speed) {
	throw new NotImplementedException();
    }

    public Short getPosition(int servoId) {
	throw new NotImplementedException();
    }

    public Short getMaxPosition(int servoId) {
	throw new NotImplementedException();
    }

    public void setMaxPosition(int servoId, short pos) {
	throw new NotImplementedException();
    }

    public Short getMinPosition(int servoId) {
	throw new NotImplementedException();
    }

    public void setMinPosition(int servoId, short pos) {
	throw new NotImplementedException();
    }

    public void setAngleOffset(int servoId, byte offset) {
	throw new NotImplementedException();
    }

    public Byte getAngleOffset(int servoId) {
	throw new NotImplementedException();
    }

    public void setMotorEnable(int servoId) {
	throw new NotImplementedException();
    }

    public void setMotorDisable(int servoId) {
	throw new NotImplementedException();
    }

    public Boolean getMotorEnabled(int servoId) {
	throw new NotImplementedException();
    }

    public Byte getMaxTemperature(int servoId) {
	throw new NotImplementedException();
    }

    public void setMaxTemperature(int servoId, short temp) {
	throw new NotImplementedException();
    }

    public Byte getTemperature(int servoId) {
	throw new NotImplementedException();
    }

    public Double getVoltage(int servoId) {
	throw new NotImplementedException();
    }

    public Short getMinVoltage(int servoId) {
	throw new NotImplementedException();
    }

    public void setMinVoltage(int servoId, short voltage) {
	throw new NotImplementedException();
    }

    public Short getMaxVoltage(int servoId) {
	throw new NotImplementedException();
    }

    public void setMaxVoltage(int servoId, short temp) {
	throw new NotImplementedException();
    }

    public Boolean getLedState(int servoId) {
	throw new NotImplementedException();
    }

    public void setLedState(int servoId, boolean state) {
	throw new NotImplementedException();
    }

    public Byte getLedErrorState(int id) {
	throw new NotImplementedException();
    }

    public void setLedErrorState(int id, byte state) {
	throw new NotImplementedException();
    }

    public abstract Boolean hasVoltage();

    public abstract Boolean hasTemperature();

    public abstract Boolean hasMinPosition();

    public abstract Boolean hasMaxPosition();

    public abstract Boolean hasLed();
}
