package com.github.tand0.naiview.adapter;

import android.net.Uri;

/**
 * URI etc
 * @author T.Ando
 */
public class UriEtc {
    public UriEtc(Uri uri, String mime) {
        this.uri = uri;
        this.mime = mime;
    }
    public Uri uri;
    public String mime;
    @Override
    public boolean equals(Object o) {
        if (o instanceof UriEtc) {
            return this.uri.equals(((UriEtc) o).uri);
        }
        return super.equals(o);
    }
}
