package it.feio.android.omninotes.helpers

import android.content.Context
import org.junit.Test

class GeocodeProviderBaseFactoryTest  {
    private Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void testGetLocationMode() {
        int k= GeocodeProviderBaseFactory.getLocationMode(context);
        assertThat(k).isNotEqualTo(-1);
    }
}
