package com.megaache.xmslocationmanager;

import android.os.Parcel;

import org.xms.g.location.LocationRequest;

/**
 * A data object that contains quality of service parameters for requests to the FusedLocationProviderApi.<br/>
 */
public class XMSLocationRequest implements android.os.Parcelable {
    public static final int PRIORITY_HIGH_ACCURACY = 100;
    public static final int PRIORITY_BALANCED_POWER_ACCURACY = 102;
    public static final int PRIORITY_LOW_POWER = 104;
    public static final int PRIORITY_NO_POWER = 105;
    private LocationRequest request;

    /**
     * android.os.Parcelable.Creator.CREATOR a public CREATOR field that generates instances of your Parcelable class from a Parcel.<br/>
     * <p>
     */

    private XMSLocationRequest(LocationRequest request) {
        this.request = request;
    }

    public XMSLocationRequest() {
        this.request = LocationRequest.create();
    }

    public static final android.os.Parcelable.Creator CREATOR = new android.os.Parcelable.Creator() {
        public  XMSLocationRequest createFromParcel(Parcel param0) {
            LocationRequest request = (LocationRequest) LocationRequest.CREATOR.createFromParcel(param0);
            return new XMSLocationRequest(request);
        }

        public XMSLocationRequest[] newArray(int param0) {
            return new XMSLocationRequest[param0];
        }
    };


    /**
     * Create a location request with default parameters.<br/>
     *
     * @return a new location request
     */
    public static XMSLocationRequest create() {
        return new XMSLocationRequest(LocationRequest.create());
    }

    /**
     * Checks whether two instances are equal.<br/>
     *
     * @param param0 the other instance
     * @return true if two instances are equal
     */
    public boolean equals(java.lang.Object param0) {
        if (param0 == this) return true;
        if (param0 instanceof XMSLocationRequest) {
            return request.equals(((XMSLocationRequest) param0).request);
        }
        return false;
    }

     public LocationRequest getRequest() {
        return request;
    }

    /**
     * Get the request expiration time, in milliseconds since boot.<br/>
     *
     * @return expiration time of request, in milliseconds since boot including suspend
     */
    public final long getExpirationTime() {
        return request.getExpirationTime();
    }

    /**
     * Get the fastest interval of this request, in milliseconds.<br/>
     *
     * @return fastest interval in milliseconds, exact
     */
    public final long getFastestInterval() {
        return request.getFastestInterval();
    }

    /**
     * Get the desired interval of this request, in milliseconds.<br/>
     *
     * @return desired interval in milliseconds, inexact
     */
    public final long getInterval() {
        return request.getInterval();
    }

    /**
     * Gets the maximum wait time in milliseconds for location updates.<br/>
     *
     * @return maximum wait time in milliseconds, inexact
     */
    public final long getMaxWaitTime() {
        return request.getMaxWaitTime();
    }

    /**
     * Get the number of updates requested.<br/>
     *
     * @return number of updates
     */
    public final int getNumUpdates() {
        return getNumUpdates();
    }

    /**
     * Get the quality of the request.<br/>
     *
     * @return an accuracy constant
     */
    public final int getPriority() {
        return request.getPriority();
    }

    /**
     * Get the minimum displacement between location updates in meters.<br/>
     *
     * @return minimum displacement between location updates in meters
     */
    public final float getSmallestDisplacement() {
        return request.getSmallestDisplacement();
    }

    /**
     * Overrides the method of the java.lang.Object class to calculate hashCode of a object.<br/>
     *
     * @return a hash code value
     */
    public final int hashCode() {
        return request.hashCode();
    }

    /**
     * Returns whether or not the fastest interval was explicitly specified for the location request.<br/>
     *
     * @return True if the fastest interval was explicitly set for the location request; false otherwise
     */
    public final boolean isFastestIntervalExplicitlySet() {
        return request.isFastestIntervalExplicitlySet();
    }

    /**
     * Set the duration of this request, in milliseconds.<br/>
     *
     * @param param0 duration of request in milliseconds
     * @return the same object, so that setters can be chained
     */
    public final XMSLocationRequest setExpirationDuration(long param0) {
        request.setExpirationDuration(param0);
        return this;
    }

    /**
     * Set the request expiration time, in millisecond since boot.<br/>
     *
     * @param param0 expiration time of request, in milliseconds since boot including suspend
     * @return the same object, so that setters can be chained
     */
    public final XMSLocationRequest setExpirationTime(long param0) {
        request.setExpirationTime(param0);
        return this;
    }

    /**
     * Explicitly set the fastest interval for location updates, in milliseconds.<br/>
     *
     * @param param0 fastest interval for updates in milliseconds, exact
     * @return the same object, so that setters can be chained
     */
    public final XMSLocationRequest setFastestInterval(long param0) {
        request.setFastestInterval(param0);
        return this;
    }

    /**
     * Set the desired interval for active location updates, in milliseconds.<br/>
     *
     * @param param0 desired interval in millisecond, inexact
     * @return the same object, so that setters can be chained
     */
    public final XMSLocationRequest setInterval(long param0) {
        request.setInterval(param0);
        return this;
    }

    /**
     * Sets the maximum wait time in milliseconds for location updates.<br/>
     *
     * @param param0 desired maximum wait time in millisecond, inexact
     * @return the same object, so that setters can be chained
     */
    public final XMSLocationRequest setMaxWaitTime(long param0) {
        request.setMaxWaitTime(param0);
        return this;
    }

    /**
     * Set the number of location updates.<br/>
     *
     * @param param0 the number of location updates requested
     * @return the same object, so that setters can be chained
     */
    public final XMSLocationRequest setNumUpdates(int param0) {
        request.setNumUpdates(param0);
        return this;
    }

    /**
     * Set the priority of the request.<br/>
     * @param param0 an accuracy or power constant
     * @return the same object, so that setters can be chained
     */
    public final XMSLocationRequest setPriority(int param0) {
        request.setPriority(param0);
        return this;
    }

    /**
     * Set the minimum displacement between location updates in meters.<br/>
     * @param param0 the smallest displacement in meters the user must move between location updates
     * @return the same object, so that setters can be chained
     */
    public final XMSLocationRequest setSmallestDisplacement(float param0) {
        request.setSmallestDisplacement(param0);
        return this;
    }

    /**
     * Overrides the method of the java.lang.Object class to convert a value into a character string.<br/>
     * @return A character string after being converted
     */
    public final java.lang.String toString() {
        return request.toString();
    }

    /**
     * Used in serialization and deserialization.<br/>
     *
     * @param param0 Parcel to which this object is written
     * @param param1 Writing mode
     */
    public void writeToParcel(android.os.Parcel param0, int param1) {
        request.writeToParcel(param0, param1);
    }

    /**
     * XMS does not provide this api.<br/>
     */
    public int describeContents() {
        throw new java.lang.RuntimeException("Not Supported");
    }

    /**
     * dynamic cast the input object to XMSLocationRequest.<br/>
     *
     * @param param0 the input object
     * @return casted LocationRequest object
     */
    public static XMSLocationRequest dynamicCast(java.lang.Object param0) {
        return ((XMSLocationRequest) param0);
    }

    /**
     * judge whether the Object is XMS instance or not.<br/>
     *
     * @param param0 the input object
     * @return true if the Object is XMS instance, otherwise false
     */
    public static boolean isInstance(java.lang.Object param0) {
        if (param0 instanceof XMSLocationRequest) return true;
        return LocationRequest.isInstance(param0);
    }

}
