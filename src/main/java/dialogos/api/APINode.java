package dialogos.api;

import com.clt.diamant.*;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.ListType;
import com.clt.script.exp.types.StructType;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class APINode extends Node
{
    private String responsetypes;
    private String requesttypes;
    private String request;
    private String returnvariable;

    private final String REQUEST = "request";
    private final String REQUEST_TYPES = "requestTypes";
    private final String RESPONSE_TYPES = "responseTypes";
    private final String RETURN_VARIABLE = "returnVariable";
    private final String[] rTypes = new String[]{"JSON"};

    public APINode()
    {
        super();
        addEdge();
    }

    public static Color getDefaultColor()
    {
        return new Color(0, 111, 222);
    }

    @Override
    public Node execute(WozInterface wozInterface, InputCenter input, ExecutionLogger logger)
    {
        request = (String) properties.get(REQUEST);
        requesttypes = (String) properties.get(REQUEST_TYPES);
        responsetypes = (String) properties.get(RESPONSE_TYPES);
        returnvariable = (String) properties.get(RETURN_VARIABLE);

        executeAPI();

        Node target = this.getEdge(0).getTarget();
        wozInterface.transition(this, target, 0, null);
        return target;
    }

    private void executeAPI()
    {
        Object result;

        final List<Slot> vars = this.getGraph().getAllVariables(Graph.LOCAL);
        Slot returnVar = null;
        for (Slot slot : vars)
        {
            if (slot.getName().equals(returnvariable))
            {
                returnVar = slot;
                break;
            }
        }

        try
        {
            String apiRequest = null;

            if (requesttypes.equals("Expression"))
            {
                Expression expression = parseExpression(request);
                Value value = expression.evaluate();
                if (value != null)
                {
                    apiRequest = value.toString();
                }
            }
            else
            {
                apiRequest = request;
            }

            if (apiRequest == null)
            {
                return;
            }

            //
            // Handle json api
            //
            if (responsetypes.equals(rTypes[0]))
            {
                result = APIInteraction.getJSON(apiRequest);
                if (result == null || returnVar == null)
                {
                    return;
                }

                if (result instanceof JSONObject && returnVar.getType() instanceof StructType)
                {
                    returnVar.setValue(Value.fromJson((JSONObject) result));
                }
                else if (result instanceof JSONArray && returnVar.getType() instanceof ListType)
                {
                    returnVar.setValue(Value.fromJson((JSONArray) result));
                }
            }
//            else if (responsetypes.equals(responseTypes[1]))
//            {
//                // Handle e.g. XML
//            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map)
    {
        super.writeAttributes(out, uid_map);

        request = (String) this.getProperty(REQUEST);
        requesttypes = (String) this.getProperty(REQUEST_TYPES);
        responsetypes = (String) this.getProperty(RESPONSE_TYPES);
        returnvariable = (String) this.getProperty(RETURN_VARIABLE);

        if (request != null)
        {
            Graph.printAtt(out, REQUEST, request);
        }
        if (requesttypes != null)
        {
            Graph.printAtt(out, REQUEST_TYPES, requesttypes);
        }
        if (responsetypes != null)
        {
            Graph.printAtt(out, RESPONSE_TYPES, responsetypes);
        }
        if (returnvariable != null)
        {
            Graph.printAtt(out, RETURN_VARIABLE, returnvariable);
        }
    }

    @Override
    public void writeVoiceXML(XMLWriter w, IdMap uid_map)
    {

    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException
    {
        switch (name)
        {
            case REQUEST:
            case REQUEST_TYPES:
            case RESPONSE_TYPES:
            case RETURN_VARIABLE:
                this.setProperty(name, value);
                break;
            default:
                super.readAttribute(r, name, value, uid_map);
        }
    }

    @Override
    public JComponent createEditorComponent(Map<String, Object> properties)
    {
        JPanel topPanel = new JPanel(new BorderLayout());

        topPanel.setBorder(new EmptyBorder(3, 3, 3, 3));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        Component requestEditor = NodePropertiesDialog.createTextArea(properties, REQUEST);

        JRadioButton[] typeButtons = NodePropertiesDialog.createRadioButtons(
                properties, REQUEST_TYPES, new String[]{"Plain Text", "Expression"});

        JPanel types = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JRadioButton button : typeButtons)
        {
            types.add(button);
        }
        typeButtons[0].setSelected(true);

        JRadioButton[] responseButtons = NodePropertiesDialog.createRadioButtons(
                properties, RESPONSE_TYPES, rTypes);
        JPanel responses = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JRadioButton button : responseButtons)
        {
            responses.add(button);
        }
        responseButtons[0].setSelected(true);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(new JLabel("Request Type:"), constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 3;
        constraints.anchor = GridBagConstraints.LINE_END;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(types, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(new JLabel("Response Type:"), constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 3;
        constraints.anchor = GridBagConstraints.LINE_END;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(responses, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_END;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(new JLabel("API Request:"), constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 3;
        constraints.anchor = GridBagConstraints.LINE_END;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(requestEditor, constraints);

        topPanel.add(inputPanel, BorderLayout.CENTER);

        final List<Slot> vars = this.getGraph().getAllVariables(Graph.LOCAL);
        vars.removeIf(slot -> (!(slot.getType() instanceof StructType) && !(slot.getType() instanceof ListType)));
        String[] varNames = new String[vars.size()];
        for (int inx = 0; inx < vars.size(); inx++)
        {
            varNames[inx] = vars.get(inx).getName();
        }

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(new JLabel("Return variable:"), constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 3;
        constraints.anchor = GridBagConstraints.LINE_END;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(NodePropertiesDialog.createComboBox(properties, RETURN_VARIABLE, varNames), constraints);

        return topPanel;
    }

    @Override
    public boolean editProperties(Component parent)
    {
        Map<String, Object> props = (Map<String, Object>) this.deep_copy(this.properties);

        NodePropertiesDialog dialog = new NodePropertiesDialog(this, parent, props, this.createEditorComponent(props));
        dialog.setVisible(true);

        this.setProperty(NodePropertiesDialog.LAST_TAB, props.get(NodePropertiesDialog.LAST_TAB));
        this.setProperty(NodePropertiesDialog.LAST_SIZE, props.get(NodePropertiesDialog.LAST_SIZE));
        this.setProperty(NodePropertiesDialog.LAST_POSITION, props.get(NodePropertiesDialog.LAST_POSITION));

        if (dialog.approved())
        {
            for (String key : props.keySet())
            {
                if (!key.equals("numEdges"))
                {
                    this.setProperty(key, props.get(key));
                }
            }

            this.properties.keySet().removeIf(key -> !props.containsKey(key));

            return true;
        }
        else
        {
            return false;
        }
    }
}