package jp.ne.ruru.park.ando.naiview.adapter;

import android.net.Uri;


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
