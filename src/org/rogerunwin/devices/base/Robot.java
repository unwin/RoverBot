/**
 * 
 */
package org.rogerunwin.devices.base;

/**
 * @author brain
 *
 */
public interface Robot {
	public void GetFootprint();	
	public void SupportsLidar();	
	public void SupportsDepthCamera();	
	public void GetMovementStyle();	
}
