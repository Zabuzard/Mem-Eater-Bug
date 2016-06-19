package de.zabuza.memeaterbug.winapi.jna.util;

import com.sun.jna.Pointer;

import de.zabuza.memeaterbug.winapi.jna.Shell32;

public abstract class Shell32Util
{
    public static Pointer ExtractSmallIcon(String lpszFile, int nIconIndex)
    {
        Pointer[] hIcons = new Pointer[1];
        Shell32.INSTANCE.ExtractIconEx(lpszFile, 0, null, hIcons, nIconIndex);
        return hIcons[0];
    }
    
}
