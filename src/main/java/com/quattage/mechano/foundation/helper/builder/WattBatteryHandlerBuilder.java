package com.quattage.mechano.foundation.helper.builder;

import com.quattage.mechano.foundation.electricity.WattBatteryHandler;
import com.quattage.mechano.foundation.electricity.WattBatteryHandlable;
import com.quattage.mechano.foundation.block.orientation.Relative;
import com.quattage.mechano.foundation.electricity.PowerJunction;
import com.quattage.mechano.foundation.electricity.watt.WattBattery;
import com.quattage.mechano.foundation.electricity.watt.WattBatteryBuilder;
import com.quattage.mechano.foundation.electricity.watt.WattBatteryBuilder.WattBatteryUnbuilt;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import java.util.ArrayList;
import java.util.function.Function;

/***
 * A fluent builder for BatteryBanks
 */
public class WattBatteryHandlerBuilder<T extends SmartBlockEntity & WattBatteryHandlable> {
    
    private Function<WattBatteryBuilder, WattBatteryUnbuilt> bFunc;
    private T target = null;
    private ArrayList<PowerJunction> interactions = new ArrayList<PowerJunction>();

    public WattBatteryHandlerBuilder() {}

    /**
     * Bind this BatteryBank to a given BlockEntity.
     * @param target BlockEntity to bind this BatteryBank to
     * @return This BatteryBankBuilder, modified to reflect this change.
     */
    public WattBatteryHandlerBuilder<T> at(T target) {
        this.target = target;
        return this;
    }

    /**
     * Define the characteristics of the resulting BatteryBank's underlying WattBattery object. Example:
     * <pre>
     * .defineBattery(b -> b
    .withFlux(120)
    .withVoltageTolerance(120)
    .withMaxCharge(2048)
    .withMaxDischarge(2048)
    .withCapacity(2048)
    .withOvervoltBehavior(OvervoltBehavior.LIMIT_LOSSY)
.withNoEvent())
        </pre>
     * @param bFunc Lambda defining your battery's characteristics
     * @return This BatteryBankBuilder, modified to reflect this change.
     */
    public WattBatteryHandlerBuilder<T> defineBattery(Function<WattBatteryBuilder, WattBatteryUnbuilt> bFunc) {
        this.bFunc = bFunc;
        return this;
    }

    public PowerJunctionBuilder<T> newInteraction(Relative rel) {
        return new PowerJunctionBuilder<T>(this, rel);
    }

    public WattBatteryHandlerBuilder<T> interactsWithAllSides() {
        for(Relative rel: Relative.values())
            interactions.add(new PowerJunction(rel));
        return this;
    }

    public WattBatteryHandler<T> build() {
        if(interactions == null) new WattBatteryHandler<T>(target, null, bFunc.apply(WattBattery.newBattery()));
        return new WattBatteryHandler<T>(target, interactions.toArray(new PowerJunction[0]), bFunc.apply(WattBattery.newBattery()));
    }

    /***
     * Add an InteractionPolicy directly. This is designed to be used
     * by the builder, you don't have to call it yourself.
     */
    protected void addInteraction(PowerJunction p) {
        if(p == null) return;
        interactions.add(p);
    }
}