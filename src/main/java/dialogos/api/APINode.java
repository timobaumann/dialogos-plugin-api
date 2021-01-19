package dialogos.api;

import com.clt.diamant.*;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.StructType;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class APINode extends Node
{
    private String RESPONSETYPE;
    private String REQUESTTYPE;
    private String REQUEST;
    private String RETURNVARIABLE;

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
        executeAPI();

        Node target = this.getEdge(0).getTarget();
        wozInterface.transition(this, target, 0, null);
        return target;
    }

    private void executeAPI()
    {
        JSONObject result = null;

        try
        {
            String apiRequest = null;

            if (REQUESTTYPE.equals("Expression"))
            {
                Expression expression = parseExpression(REQUEST);
                Value value = expression.evaluate();
                if (value != null)
                {
                    apiRequest = value.toString();
                }
            }
            else
            {
                apiRequest = REQUEST;
            }

            if (apiRequest == null)
            {
                return;
            }

            switch (RESPONSETYPE)
            {
                case "JSON":
                    result = APIInteraction.getJSON(apiRequest);
                    break;
//                case "XML":
//                    return;
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        final List<Slot> vars = this.getGraph().getAllVariables(Graph.LOCAL);
        Slot returnVar = null;
        for (Slot slot : vars)
        {
            if (slot.getName().equals(RETURNVARIABLE))
            {
                returnVar = slot;
                break;
            }
        }

        if (result == null || returnVar == null)
        {
            return;
        }

        returnVar.setValue(Value.fromJson(result));
    }

    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map)
    {
        super.writeAttributes(out, uid_map);

        REQUEST = (String) this.getProperty("request");
        REQUESTTYPE = (String) this.getProperty("requestTypes");
        RESPONSETYPE = (String) this.getProperty("responseTypes");
        RETURNVARIABLE = (String) this.getProperty("returnVariable");

        if (REQUEST != null)
        {
            Graph.printAtt(out, "request", REQUEST);
        }
        if (REQUESTTYPE != null)
        {
            Graph.printAtt(out, "requestTypes", REQUESTTYPE);
        }
        if (RESPONSETYPE != null)
        {
            Graph.printAtt(out, "responseTypes", RESPONSETYPE);
        }
        if (RETURNVARIABLE != null)
        {
            Graph.printAtt(out, "returnVariable", RETURNVARIABLE);
        }
    }

    @Override
    public void writeVoiceXML(XMLWriter w, IdMap uid_map) throws IOException
    {

    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException
    {
        switch (name)
        {
            case "request":
            case "requestTypes":
            case "responseTypes":
            case "returnVariable":
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

        Component requestEditor = NodePropertiesDialog.createTextArea(properties, "request");

        JRadioButton[] typeButtons = NodePropertiesDialog.createRadioButtons(
                properties, "requestTypes", new String[]{"Plain Text", "Expression"});

        JPanel types = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JRadioButton button : typeButtons)
        {
            types.add(button);
        }
        typeButtons[0].setSelected(true);

        JRadioButton[] responseButtons = NodePropertiesDialog.createRadioButtons(
                properties, "responseTypes", new String[]{"JSON"});
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
        vars.removeIf(slot -> !slot.getType().getClass().getName().equals(StructType.class.getName()));
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
        inputPanel.add(NodePropertiesDialog.createComboBox(properties, "returnVariable", varNames), constraints);

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

            REQUEST = (String) properties.get("request");
            REQUESTTYPE = (String) properties.get("requestTypes");
            RESPONSETYPE = (String) properties.get("responseTypes");
            RETURNVARIABLE = (String) properties.get("returnVariable");

            this.properties.keySet().removeIf(key -> !props.containsKey(key));

            return true;
        }
        else
        {
            return false;
        }
    }
}