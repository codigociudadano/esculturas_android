package pl.polidea.coverflow;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jmv.codigociudadano.resistenciarte.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

/**
 * This class is an adapter that provides images from a fixed set of resource
 * ids. Bitmaps and ImageViews are kept as weak references so that they can be
 * cleared by garbage collection when not needed.
 * 
 */
public class ResourceImageAdapter extends AbstractCoverFlowImageAdapter {

    /** The Constant TAG. */
    private static final String TAG = ResourceImageAdapter.class.getSimpleName();

    /** The Constant DEFAULT_LIST_SIZE. */
    private static final int DEFAULT_LIST_SIZE = 20;

    /** The Constant IMAGE_RESOURCE_IDS. */
    private static final ArrayList<Bitmap> IMAGE_RESOURCE_IDS = new ArrayList<Bitmap>(DEFAULT_LIST_SIZE);

    /** The Constant DEFAULT_RESOURCE_LIST. */
    private static ArrayList<Bitmap> DEFAULT_RESOURCE_LIST;
    /** The bitmap map. */
    private final Map<Integer, WeakReference<Bitmap>> bitmapMap = new HashMap<Integer, WeakReference<Bitmap>>();

    private final Context context;

    /**
     * Creates the adapter with default set of resource images.
     * 
     * @param context
     *            context
     * @param bd 
     */
    public ResourceImageAdapter(final Context context, ArrayList<Bitmap> bd) {
        super();
        this.context = context;
        setResources(bd);
    }

    /**
     * Replaces resources with those specified.
     * 
     * @param resourceIds
     *            array of ids of resources.
     */
    public final synchronized void setResources(final ArrayList<Bitmap> resourceIds) {
        IMAGE_RESOURCE_IDS.clear();
        for (final Bitmap resourceId : resourceIds) {
            IMAGE_RESOURCE_IDS.add(resourceId);
        }
        notifyDataSetChanged();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public synchronized int getCount() {
        return IMAGE_RESOURCE_IDS.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see pl.polidea.coverflow.AbstractCoverFlowImageAdapter#createBitmap(int)
     */
    @Override
    protected Bitmap createBitmap(final int position) {
        Log.v(TAG, "creating item " + position);
        if (position < IMAGE_RESOURCE_IDS.size()){
        	final Bitmap bitmap = IMAGE_RESOURCE_IDS.get(position);
        	bitmapMap.put(position, new WeakReference<Bitmap>(bitmap));
            return bitmap;
        }
        return null;
    }
}