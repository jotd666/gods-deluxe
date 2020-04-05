#include <windows.h>
#include <limits.h>

#include "JoyStick.h"

#define MAX_VALUE	(int)(USHRT_MAX / 2)

static int joy_error = 0;

/* JOTD: if joystick not plugged in, this takes too long to
   detect and the game crawls or freezes!!
   I set the error flag so if joystick not detected, then the
   error is forever */
   
static int joy_get_pos(int id, JOYINFO *info)
{    
    if (joy_error == 0)
    {
	if (joyGetPos(id + JOYSTICKID1, info) != JOYERR_NOERROR)
	{
	    joy_error = -1;
	}
    }

    return joy_error;
}

/*
 * Class:     Joystick
 * Method:    getButton
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_joystick_Joystick_getButtons(
JNIEnv	*env,
jobject	obj,
jint	id)
{
	JOYINFO	info;
	
	if (joy_get_pos(id, &info) < 0)
		return 0;

	int button = 0;
	if (info.wButtons & JOY_BUTTON1)
		button |= Joystick_BUTTON1;
	if (info.wButtons & JOY_BUTTON2)
		button |= Joystick_BUTTON2;
	if (info.wButtons & JOY_BUTTON3)
		button |= Joystick_BUTTON3;
	if (info.wButtons & JOY_BUTTON4)
		button |= Joystick_BUTTON4;

	return button;
}

/*
 * Class:     Joystick
 * Method:    getNumDevs
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_joystick_Joystick_getNumDevs(
JNIEnv	*env,
jobject	obj)
{
	return joyGetNumDevs();
}

/*
 * Class:     Joystick
 * Method:    getXPos
 * Signature: (I)I
 */
JNIEXPORT jfloat JNICALL Java_joystick_Joystick_getXPos(
JNIEnv	*env,
jobject	obj,
jint	id)
{
	JOYINFO	info;
	if (joy_get_pos(id, &info) < 0)
		return 0.0f;
	return (float)((int)info.wXpos - MAX_VALUE) / (float)MAX_VALUE;
}

/*
 * Class:     Joystick
 * Method:    getYPos
 * Signature: (I)I
 */
JNIEXPORT jfloat JNICALL Java_joystick_Joystick_getYPos(
JNIEnv	*env,
jobject	obj,
jint	id)
{
	JOYINFO	info;
	if (joy_get_pos(id, &info) < 0)
		return 0.0f;
	return (float)((int)info.wYpos - MAX_VALUE) / (float)MAX_VALUE;
}

/*
 * Class:     Joystick
 * Method:    getZPos
 * Signature: (I)I
 */
JNIEXPORT jfloat JNICALL Java_joystick_Joystick_getZPos(
JNIEnv	*env,
jobject	obj,
jint	id)
{
	JOYINFO	info;
	if (joy_get_pos(id, &info) < 0)
		return 0.0f;
	return (float)((int)info.wZpos - MAX_VALUE) / (float)MAX_VALUE;
}

