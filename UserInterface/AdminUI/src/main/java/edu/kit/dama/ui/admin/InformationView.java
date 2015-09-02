/* 
 * Copyright 2015 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.ui.admin;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.tools.DigitalObjectSecureQueryHelper;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import java.text.NumberFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public class InformationView extends CustomComponent {

  private static final Logger LOGGER = LoggerFactory.getLogger(InformationView.class);

  private final AdminUIMainView parent;
  private VerticalLayout mainLayout;
  private Table table;
//  private final Diagram diagram;

  /**
   * Default constructor.
   *
   * @param pParent The parent view.
   */
  public InformationView(AdminUIMainView pParent) {
    parent = pParent;
    buildMainLayout();
    //just a test
//    diagram = new Diagram();
//
//    String data = "{\"name\":\"Successful\",\"children\":[{\"name\":\"analytics\",\"children\":[{\"name\":\"cluster\",\"children\":[{\"name\":\"AgglomerativeCluster\",\"value\":3938},{\"name\":\"CommunityStructure\",\"value\":3812},{\"name\":\"HierarchicalCluster\",\"value\":6714},{\"name\":\"MergeEdge\",\"value\":743}]},{\"name\":\"graph\",\"children\":[{\"name\":\"BetweennessCentrality\",\"value\":3534},{\"name\":\"LinkDistance\",\"value\":5731},{\"name\":\"MaxFlowMinCut\",\"value\":7840},{\"name\":\"ShortestPaths\",\"value\":5914},{\"name\":\"SpanningTree\",\"value\":3416}]},{\"name\":\"optimization\",\"children\":[{\"name\":\"AspectRatioBanker\",\"value\":7074}]}]},{\"name\":\"animate\",\"children\":[{\"name\":\"Easing\",\"value\":17010},{\"name\":\"FunctionSequence\",\"value\":5842},{\"name\":\"interpolate\",\"children\":[{\"name\":\"ArrayInterpolator\",\"value\":1983},{\"name\":\"ColorInterpolator\",\"value\":2047},{\"name\":\"DateInterpolator\",\"value\":1375},{\"name\":\"Interpolator\",\"value\":8746},{\"name\":\"MatrixInterpolator\",\"value\":2202},{\"name\":\"NumberInterpolator\",\"value\":1382},{\"name\":\"ObjectInterpolator\",\"value\":1629},{\"name\":\"PointInterpolator\",\"value\":1675},{\"name\":\"RectangleInterpolator\",\"value\":2042}]},{\"name\":\"ISchedulable\",\"value\":1041},{\"name\":\"Parallel\",\"value\":5176},{\"name\":\"Pause\",\"value\":449},{\"name\":\"Scheduler\",\"value\":5593},{\"name\":\"Sequence\",\"value\":5534},{\"name\":\"Transition\",\"value\":9201},{\"name\":\"Transitioner\",\"value\":19975},{\"name\":\"TransitionEvent\",\"value\":1116},{\"name\":\"Tween\",\"value\":6006}]},{\"name\":\"data\",\"children\":[{\"name\":\"converters\",\"children\":[{\"name\":\"Converters\",\"value\":721},{\"name\":\"DelimitedTextConverter\",\"value\":4294},{\"name\":\"GraphMLConverter\",\"value\":9800},{\"name\":\"IDataConverter\",\"value\":1314},{\"name\":\"JSONConverter\",\"value\":2220}]},{\"name\":\"DataField\",\"value\":1759},{\"name\":\"DataSchema\",\"value\":2165},{\"name\":\"DataSet\",\"value\":586},{\"name\":\"DataSource\",\"value\":3331},{\"name\":\"DataTable\",\"value\":772},{\"name\":\"DataUtil\",\"value\":3322}]},{\"name\":\"display\",\"children\":[{\"name\":\"DirtySprite\",\"value\":8833},{\"name\":\"LineSprite\",\"value\":1732},{\"name\":\"RectSprite\",\"value\":3623},{\"name\":\"TextSprite\",\"value\":10066}]},{\"name\":\"flex\",\"children\":[{\"name\":\"FlareVis\",\"value\":4116}]},{\"name\":\"physics\",\"children\":[{\"name\":\"DragForce\",\"value\":1082},{\"name\":\"GravityForce\",\"value\":1336},{\"name\":\"IForce\",\"value\":319},{\"name\":\"NBodyForce\",\"value\":10498},{\"name\":\"Particle\",\"value\":2822},{\"name\":\"Simulation\",\"value\":9983},{\"name\":\"Spring\",\"value\":2213},{\"name\":\"SpringForce\",\"value\":1681}]},{\"name\":\"query\",\"children\":[{\"name\":\"AggregateExpression\",\"value\":1616},{\"name\":\"And\",\"value\":1027},{\"name\":\"Arithmetic\",\"value\":3891},{\"name\":\"Average\",\"value\":891},{\"name\":\"BinaryExpression\",\"value\":2893},{\"name\":\"Comparison\",\"value\":5103},{\"name\":\"CompositeExpression\",\"value\":3677},{\"name\":\"Count\",\"value\":781},{\"name\":\"DateUtil\",\"value\":4141},{\"name\":\"Distinct\",\"value\":933},{\"name\":\"Expression\",\"value\":5130},{\"name\":\"ExpressionIterator\",\"value\":3617},{\"name\":\"Fn\",\"value\":3240},{\"name\":\"If\",\"value\":2732},{\"name\":\"IsA\",\"value\":2039},{\"name\":\"Literal\",\"value\":1214},{\"name\":\"Match\",\"value\":3748},{\"name\":\"Maximum\",\"value\":843},{\"name\":\"methods\",\"children\":[{\"name\":\"add\",\"value\":593},{\"name\":\"and\",\"value\":330},{\"name\":\"average\",\"value\":287},{\"name\":\"count\",\"value\":277},{\"name\":\"distinct\",\"value\":292},{\"name\":\"div\",\"value\":595},{\"name\":\"eq\",\"value\":594},{\"name\":\"fn\",\"value\":460},{\"name\":\"gt\",\"value\":603},{\"name\":\"gte\",\"value\":625},{\"name\":\"iff\",\"value\":748},{\"name\":\"isa\",\"value\":461},{\"name\":\"lt\",\"value\":597},{\"name\":\"lte\",\"value\":619},{\"name\":\"max\",\"value\":283},{\"name\":\"min\",\"value\":283},{\"name\":\"mod\",\"value\":591},{\"name\":\"mul\",\"value\":603},{\"name\":\"neq\",\"value\":599},{\"name\":\"not\",\"value\":386},{\"name\":\"or\",\"value\":323},{\"name\":\"orderby\",\"value\":307},{\"name\":\"range\",\"value\":772},{\"name\":\"select\",\"value\":296},{\"name\":\"stddev\",\"value\":363},{\"name\":\"sub\",\"value\":600},{\"name\":\"sum\",\"value\":280},{\"name\":\"update\",\"value\":307},{\"name\":\"variance\",\"value\":335},{\"name\":\"where\",\"value\":299},{\"name\":\"xor\",\"value\":354},{\"name\":\"_\",\"value\":264}]},{\"name\":\"Minimum\",\"value\":843},{\"name\":\"Not\",\"value\":1554},{\"name\":\"Or\",\"value\":970},{\"name\":\"Query\",\"value\":13896},{\"name\":\"Range\",\"value\":1594},{\"name\":\"StringUtil\",\"value\":4130},{\"name\":\"Sum\",\"value\":791},{\"name\":\"Variable\",\"value\":1124},{\"name\":\"Variance\",\"value\":1876},{\"name\":\"Xor\",\"value\":1101}]},{\"name\":\"scale\",\"children\":[{\"name\":\"IScaleMap\",\"value\":2105},{\"name\":\"LinearScale\",\"value\":1316},{\"name\":\"LogScale\",\"value\":3151},{\"name\":\"OrdinalScale\",\"value\":3770},{\"name\":\"QuantileScale\",\"value\":2435},{\"name\":\"QuantitativeScale\",\"value\":4839},{\"name\":\"RootScale\",\"value\":1756},{\"name\":\"Scale\",\"value\":4268},{\"name\":\"ScaleType\",\"value\":1821},{\"name\":\"TimeScale\",\"value\":5833}]},{\"name\":\"util\",\"children\":[{\"name\":\"Arrays\",\"value\":8258},{\"name\":\"Colors\",\"value\":10001},{\"name\":\"Dates\",\"value\":8217},{\"name\":\"Displays\",\"value\":12555},{\"name\":\"Filter\",\"value\":2324},{\"name\":\"Geometry\",\"value\":10993},{\"name\":\"heap\",\"children\":[{\"name\":\"FibonacciHeap\",\"value\":9354},{\"name\":\"HeapNode\",\"value\":1233}]},{\"name\":\"IEvaluable\",\"value\":335},{\"name\":\"IPredicate\",\"value\":383},{\"name\":\"IValueProxy\",\"value\":874},{\"name\":\"math\",\"children\":[{\"name\":\"DenseMatrix\",\"value\":3165},{\"name\":\"IMatrix\",\"value\":2815},{\"name\":\"SparseMatrix\",\"value\":3366}]},{\"name\":\"Maths\",\"value\":17705},{\"name\":\"Orientation\",\"value\":1486},{\"name\":\"palette\",\"children\":[{\"name\":\"ColorPalette\",\"value\":6367},{\"name\":\"Palette\",\"value\":1229},{\"name\":\"ShapePalette\",\"value\":2059},{\"name\":\"SizePalette\",\"value\":2291}]},{\"name\":\"Property\",\"value\":5559},{\"name\":\"Shapes\",\"value\":19118},{\"name\":\"Sort\",\"value\":6887},{\"name\":\"Stats\",\"value\":6557},{\"name\":\"Strings\",\"value\":22026}]},{\"name\":\"vis\",\"children\":[{\"name\":\"axis\",\"children\":[{\"name\":\"Axes\",\"value\":1302},{\"name\":\"Axis\",\"value\":24593},{\"name\":\"AxisGridLine\",\"value\":652},{\"name\":\"AxisLabel\",\"value\":636},{\"name\":\"CartesianAxes\",\"value\":6703}]},{\"name\":\"controls\",\"children\":[{\"name\":\"AnchorControl\",\"value\":2138},{\"name\":\"ClickControl\",\"value\":3824},{\"name\":\"Control\",\"value\":1353},{\"name\":\"ControlList\",\"value\":4665},{\"name\":\"DragControl\",\"value\":2649},{\"name\":\"ExpandControl\",\"value\":2832},{\"name\":\"HoverControl\",\"value\":4896},{\"name\":\"IControl\",\"value\":763},{\"name\":\"PanZoomControl\",\"value\":5222},{\"name\":\"SelectionControl\",\"value\":7862},{\"name\":\"TooltipControl\",\"value\":8435}]},{\"name\":\"data\",\"children\":[{\"name\":\"Data\",\"value\":20544},{\"name\":\"DataList\",\"value\":19788},{\"name\":\"DataSprite\",\"value\":10349},{\"name\":\"EdgeSprite\",\"value\":3301},{\"name\":\"NodeSprite\",\"value\":19382},{\"name\":\"render\",\"children\":[{\"name\":\"ArrowType\",\"value\":698},{\"name\":\"EdgeRenderer\",\"value\":5569},{\"name\":\"IRenderer\",\"value\":353},{\"name\":\"ShapeRenderer\",\"value\":2247}]},{\"name\":\"ScaleBinding\",\"value\":11275},{\"name\":\"Tree\",\"value\":7147},{\"name\":\"TreeBuilder\",\"value\":9930}]},{\"name\":\"events\",\"children\":[{\"name\":\"DataEvent\",\"value\":2313},{\"name\":\"SelectionEvent\",\"value\":1880},{\"name\":\"TooltipEvent\",\"value\":1701},{\"name\":\"VisualizationEvent\",\"value\":1117}]},{\"name\":\"legend\",\"children\":[{\"name\":\"Legend\",\"value\":20859},{\"name\":\"LegendItem\",\"value\":4614},{\"name\":\"LegendRange\",\"value\":10530}]},{\"name\":\"operator\",\"children\":[{\"name\":\"distortion\",\"children\":[{\"name\":\"BifocalDistortion\",\"value\":4461},{\"name\":\"Distortion\",\"value\":6314},{\"name\":\"FisheyeDistortion\",\"value\":3444}]},{\"name\":\"encoder\",\"children\":[{\"name\":\"ColorEncoder\",\"value\":3179},{\"name\":\"Encoder\",\"value\":4060},{\"name\":\"PropertyEncoder\",\"value\":4138},{\"name\":\"ShapeEncoder\",\"value\":1690},{\"name\":\"SizeEncoder\",\"value\":1830}]},{\"name\":\"filter\",\"children\":[{\"name\":\"FisheyeTreeFilter\",\"value\":5219},{\"name\":\"GraphDistanceFilter\",\"value\":3165},{\"name\":\"VisibilityFilter\",\"value\":3509}]},{\"name\":\"IOperator\",\"value\":1286},{\"name\":\"label\",\"children\":[{\"name\":\"Labeler\",\"value\":9956},{\"name\":\"RadialLabeler\",\"value\":3899},{\"name\":\"StackedAreaLabeler\",\"value\":3202}]},{\"name\":\"layout\",\"children\":[{\"name\":\"AxisLayout\",\"value\":6725},{\"name\":\"BundledEdgeRouter\",\"value\":3727},{\"name\":\"CircleLayout\",\"value\":9317},{\"name\":\"CirclePackingLayout\",\"value\":12003},{\"name\":\"DendrogramLayout\",\"value\":4853},{\"name\":\"ForceDirectedLayout\",\"value\":8411},{\"name\":\"IcicleTreeLayout\",\"value\":4864},{\"name\":\"IndentedTreeLayout\",\"value\":3174},{\"name\":\"Layout\",\"value\":7881},{\"name\":\"NodeLinkTreeLayout\",\"value\":12870},{\"name\":\"PieLayout\",\"value\":2728},{\"name\":\"RadialTreeLayout\",\"value\":12348},{\"name\":\"RandomLayout\",\"value\":870},{\"name\":\"StackedAreaLayout\",\"value\":9121},{\"name\":\"TreeMapLayout\",\"value\":9191}]},{\"name\":\"Operator\",\"value\":2490},{\"name\":\"OperatorList\",\"value\":5248},{\"name\":\"OperatorSequence\",\"value\":4190},{\"name\":\"OperatorSwitch\",\"value\":2581},{\"name\":\"SortOperator\",\"value\":2023}]},{\"name\":\"Visualization\",\"value\":16540}]}]}";
//    data = "{\n" +
//" \"name\": \"Studies\",\n" +
//" \"children\": [\n" +
//"  {\n" +
//"   \"name\": \"Study1\",\n" +
//"   \"description\":\"Test\", \n" +
//"   \"children\": [\n" +
//"	  {\"name\": \"Investigation1\",\n" +
//"	  \"description\":\"Test\", \n" +
//"	  \"children\":[\n" +
//"		 {\"name\":\"Object1\", \"description\":\"Test\", \"value\": 70},\n" +
//"		 {\"name\":\"Object2\",\"description\":\"Test\",  \"value\": 10},\n" +
//"		 {\"name\":\"Object3\", \"description\":\"Test\", \"value\": 20}\n" +
//"		]\n" +
//"		},\n" +
//"		 {\"name\": \"Investigation2\",\n" +
//"	  \"children\":[\n" +
//"		 {\"name\":\"Object4\", \"description\":\"Test\", \"value\": 50},\n" +
//"		 {\"name\":\"Object5\", \"description\":\"Test\", \"value\": 50}\n" +
//"		]\n" +
//"		}\n" +
//"	  ]\n" +
//"	  },\n" +
//"	  { \"name\": \"Study2\",\n" +
//"	  \"description\":\"Test\", \n" +
//"   \"children\": [\n" +
//"	  {\"name\": \"Investigation3\",\n" +
//"	  \"description\":\"Test\", \n" +
//"	  \"children\":[\n" +
//"		 {\"name\":\"Object8\", \"description\":\"Test\", \"value\": 12},\n" +
//"		 {\"name\":\"Object56\",\"description\":\"Test\",  \"value\": 60},\n" +
//"		 {\"name\":\"Object345\", \"description\":\"Test\", \"value\": 28}\n" +
//"		]\n" +
//"		},\n" +
//"		 {\"name\": \"Investigation4\",\n" +
//"		 \"description\":\"Test\", \n" +
//"	  \"children\":[\n" +
//"		 {\"name\":\"Object44\", \"description\":\"Test\", \"value\": 80},\n" +
//"		 {\"name\":\"Object56\", \"description\":\"Test\", \"value\": 20}\n" +
//"		]\n" +
//"		}\n" +
//"	  ]\n" +
//"	  }\n" +
//"	  ]\n" +
//"	  }\n" +
//"	 ";

//    diagram.getState().setData(data);
    setCompositionRoot(mainLayout);
//    setCompositionRoot(diagram);
    setSizeFull();
  }

  /**
   * Build the main layout.
   */
  private void buildMainLayout() {
    table = new Table();
    table.setSelectable(false);
    table.setMultiSelect(false);
    table.setImmediate(true);
    table.setWidth("600px");
    table.setHeight("400px");
    table.setColumnExpandRatio("Identifier", 1);
    table.setColumnWidth("You", 100);
    table.setColumnWidth("Global", 100);
    table.setColumnWidth("Percentage", 100);

    table.setColumnAlignment("You", Align.CENTER);
    table.setColumnAlignment("Global", Align.CENTER);
    table.setColumnAlignment("Percentage", Align.CENTER);

    table.setColumnHeader("Identifier", "");
    table.setColumnHeader("You", "Your Share");
    table.setColumnHeader("Global", "Global Amount");
    table.setColumnHeader("Percentage", "Percentage");

    Button reloadButton = new Button("Reload");
    reloadButton.addClickListener(new Button.ClickListener() {

      @Override
      public void buttonClick(Button.ClickEvent event) {
        reload();
      }
    });

    mainLayout = new VerticalLayout(table, reloadButton);
    mainLayout.setComponentAlignment(table, Alignment.MIDDLE_CENTER);
    mainLayout.setComponentAlignment(reloadButton, Alignment.MIDDLE_CENTER);
    mainLayout.setSizeFull();
    mainLayout.setMargin(true);
    mainLayout.setSpacing(true);

    reload();
  }

  protected void reload() {
    IMetaDataManager mdm = null;
    try {
      IAuthorizationContext ctx = parent.getAuthorizationContext();
      mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
      mdm.setAuthorizationContext(ctx);
      LOGGER.debug("Getting all digital objects.");
      List<DigitalObject> results = new DigitalObjectSecureQueryHelper().getReadableResources(mdm, 0, Integer.MAX_VALUE, ctx);
      LOGGER.debug("Obtained {} result(s).", results.size());

      /*  StringBuilder b = new StringBuilder();
       b.append("{\"name\":\"Studies\",\n"
       + "\"children\":[");
       for(Study s : studies){
       b.append("{\"name\":\"").append(s.getTopic()).append("\"")
       }  
       b.append("]}");*/
      LOGGER.debug("Obtaining group memberships.");
      List<GroupId> groupMemberships = GroupServiceLocal.getSingleton().membershipsOf(ctx.getUserId(), 0, Integer.MAX_VALUE, AuthorizationContext.factorySystemContext());
      LOGGER.debug("Obtained {} result(s). Obtaining groups.", groupMemberships.size());
      List<GroupId> allGroups = GroupServiceLocal.getSingleton().getAllGroupsIds(0, Integer.MAX_VALUE, AuthorizationContext.factorySystemContext());
      LOGGER.debug("Obtained {} result(s). Obtaining DataOrganization information.", allGroups.size());

      long userFileSize = 0l;
      long userFiles = 0l;
      for (DigitalObject result : results) {
        userFileSize += DataOrganizationUtils.getAssociatedDataSize(result.getDigitalObjectId());
        userFiles += DataOrganizationUtils.getAssociatedFileCount(result.getDigitalObjectId());
      }
      LOGGER.debug("Obtained {} user file(s) with a size of {} bytes. Getting global DataOrganiazation information.", userFiles, userFileSize);
      int userDigitalObjectCount = results.size();
      int globalDigitalObjectCount = ((Number) mdm.findSingleResult("SELECT COUNT(d) FROM DigitalObject d")).intValue();
      long globalFileSize = DataOrganizationUtils.getAssociatedDataSize();
      long globalFiles = DataOrganizationUtils.getAssociatedFileCount();
      LOGGER.debug("Obtained {} global file(s) with a size of {} bytes.", globalFiles, globalFileSize);

      LOGGER.debug("Setting up table data source.");
      HierarchicalContainer container = new HierarchicalContainer();
      container.addContainerProperty("Identifier", String.class, "");
      container.addContainerProperty("You", String.class, "");
      container.addContainerProperty("Global", String.class, "");
      container.addContainerProperty("Percentage", String.class, "");

      NumberFormat nf = NumberFormat.getInstance();
      nf.setMinimumFractionDigits(0);
      nf.setMaximumFractionDigits(0);
      NumberFormat nfp = NumberFormat.getPercentInstance();

      Item i = container.addItemAt(0, 0);
      i.getItemProperty("Identifier").setValue("Group Memberships");
      i.getItemProperty("You").setValue(nf.format(groupMemberships.size()));
      i.getItemProperty("Global").setValue(nf.format(allGroups.size()));
      double percent = (double) groupMemberships.size() / (double) allGroups.size();
      if (Double.isNaN(percent)) {
        percent = 0.0;
      }
      i.getItemProperty("Percentage").setValue(nfp.format(percent));

      i = container.addItemAt(1, 1);
      i.getItemProperty("Identifier").setValue("Accessible Digital Objects");
      i.getItemProperty("You").setValue(nf.format(userDigitalObjectCount));
      i.getItemProperty("Global").setValue(nf.format(globalDigitalObjectCount));
      percent = (double) userDigitalObjectCount / (double) globalDigitalObjectCount;
      if (Double.isNaN(percent)) {
        percent = 0.0;
      }
      i.getItemProperty("Percentage").setValue(nfp.format(percent));

      i = container.addItemAt(2, 2);
      i.getItemProperty("Identifier").setValue("Single Files");
      i.getItemProperty("You").setValue(nf.format(userFiles));
      i.getItemProperty("Global").setValue(nf.format(globalFiles));
      percent = (double) userFiles / (double) globalFiles;
      if (Double.isNaN(percent)) {
        percent = 0.0;
      }
      i.getItemProperty("Percentage").setValue(nfp.format(percent));

      nf.setMinimumFractionDigits(2);
      nf.setMaximumFractionDigits(2);
      i = container.addItemAt(3, 3);
      i.getItemProperty("Identifier").setValue("Occupied Diskspace");
      i.getItemProperty("You").setValue(AbstractFile.formatSize(userFileSize));
      i.getItemProperty("Global").setValue(AbstractFile.formatSize(globalFileSize));
      percent = (double) userFileSize / (double) globalFileSize;
      if (Double.isNaN(percent)) {
        percent = 0.0;
      }
      i.getItemProperty("Percentage").setValue(nfp.format(percent));
      LOGGER.debug("Setting table data source.");
      table.setContainerDataSource(container);
    } catch (AuthorizationException ex) {
      table.setContainerDataSource(new HierarchicalContainer());
      LOGGER.error("Failed to setup information view.", ex);
      new Notification("Error", "Failed to setup information view. See logfile for details.", Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
    } finally {
      if (mdm != null) {
        mdm.close();
      }
    }
  }
}
