import filters.DeviceIdFilter
import javax.inject.Inject
import play.api.http.{DefaultHttpFilters, EnabledFilters}

class Filters @Inject()(defaultFilters: EnabledFilters, log: DeviceIdFilter)
  extends DefaultHttpFilters(defaultFilters.filters :+ log: _*)