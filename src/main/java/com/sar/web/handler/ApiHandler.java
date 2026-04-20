package com.sar.web.handler;

import com.sar.service.GroupService;
import com.sar.web.http.Request;
import com.sar.web.http.Response;
import com.sar.web.http.ReplyCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ApiHandler provides RESTful JSON API for group management.
 * 
 * This handler returns JSON responses, not HTML pages.
 * The index.html page uses JavaScript to call these endpoints via AJAX.
 * 
 * Endpoints:
 * - GET /api → Returns JSON array of all groups
 * - POST /api → Creates/updates a group, returns JSON response
 * 
 * Response format should be JSON with appropriate HTTP headers.
 */
public class ApiHandler extends AbstractRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiHandler.class);
    private final GroupService groupService;

    public ApiHandler(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * Handles GET /api - Returns all groups as JSON.
     * 
     * The response should contain group data in JSON format that the
     * JavaScript in index.html can parse and display in the table.
     * 
     * Appropriate HTTP headers must be set for JSON responses.
     */
    @Override
    protected void handleGet(Request request, Response response) {
        logger.debug("GET /api - Fetching all groups");
        
        try {
            // Fetch all groups from service
            java.util.List<com.sar.model.Group> groups = groupService.getAllGroups();
            
            // Format as JSON array
            StringBuilder jsonResponse = new StringBuilder("[");
            for (int i = 0; i < groups.size(); i++) {
                com.sar.model.Group group = groups.get(i);
                if (i > 0) jsonResponse.append(",");
                
                // Append group number and open members array
                jsonResponse.append("{\"groupNumber\":\"").append(group.getGroupNumber())
                        .append("\",\"members\":[");
                
                // Add members to the array
                com.sar.model.Group.Member[] members = group.getMembers();
                for (int j = 0; j < members.length; j++) {
                    com.sar.model.Group.Member member = members[j];
                    if (member != null && (member.getNumber() != null || member.getName() != null)) {
                        if (j > 0 && members[j-1] != null) jsonResponse.append(",");
                        jsonResponse.append("{\"number\":\"").append(member.getNumber() != null ? member.getNumber() : "")
                                .append("\",\"name\":\"").append(member.getName() != null ? member.getName() : "")
                                .append("\"}");
                    }
                }
                
                // Close members array and group object
                jsonResponse.append("]}");
            }
            jsonResponse.append("]");
            String responseBody = jsonResponse.toString();
            
            // Set response
            response.setCode(ReplyCode.OK);
            response.setHeader("Content-Type", "application/json; charset=UTF-8");
            response.setHeader("Content-Length", String.valueOf(responseBody.length()));
            
            // Handle Connection header based on request
            String connectionHeader = request.headers.getHeaderValue("Connection");
            if ("keep-alive".equalsIgnoreCase(connectionHeader)) {
                response.setHeader("Connection", "keep-alive");
            } else {
                response.setHeader("Connection", "close");
            }
            
            response.setText(responseBody);
            logger.info("GET /api - Returned {} groups", groups.size());
        } catch (Exception e) {
            logger.error("Error fetching groups", e);
            String errorResponse = "{\"error\":\"Error fetching groups\"}";
            response.setCode(ReplyCode.BADREQ);
            response.setHeader("Content-Type", "application/json; charset=UTF-8");
            response.setHeader("Content-Length", String.valueOf(errorResponse.length()));
            response.setText(errorResponse);
        }
    }

    /**
     * Handles POST /api - Create or update a group.
     * 
     * The form data from index.html contains group information that
     * should be validated and persisted using the GroupService.
     * 
     * Response should be JSON indicating success or failure.
     * Appropriate HTTP headers must be set.
     */
    @Override
    protected void handlePost(Request request, Response response) {
        logger.debug("POST /api - Creating/updating group");
        
        // Students implement form data parsing, validation, and persistence
        //ir buscar os parametros do formulario
        try{
            java.util.Properties params = request.getPostParameters();
            String groupNumberStr = params.getProperty("groupNumber");
            String number0 = params.getProperty("number0");
            String name0 = params.getProperty("name0");
            String number1 = params.getProperty("number1");
            String name1 = params.getProperty("name1");
            String counterStr = params.getProperty("counter");
            //o numero nao pode estar vazio
            if(groupNumberStr == null || groupNumberStr.trim().isEmpty()) {
                String errorResponse = "{\"error\":\"O número do grupo é obrigatório\"}";
                response.setCode(ReplyCode.BADREQ);
                response.setHeader("Content-Type", "application/json; charset=UTF-8");
                response.setHeader("Content-Length", String.valueOf(errorResponse.length()));
                String connectionHeader = request.headers.getHeaderValue("Connection");
                if ("keep-alive".equalsIgnoreCase(connectionHeader)) {
                    response.setHeader("Connection", "keep-alive");
                } else {
                    response.setHeader("Connection", "close");
                }
                response.setText(errorResponse);
                return;
            }
            //converter o numero do grupo para inteiro para validar que e um numero valido
            int groupNumber = Integer.parseInt(groupNumberStr);
            //construir os arrays de numeros e nomes dos alunos, ignorando os campos vazios
            java.util.List<String> numList = new java.util.ArrayList<>();
            java.util.List<String> nameList = new java.util.ArrayList<>();

            if (number0 != null && !number0.trim().isEmpty()) {
                numList.add(number0);
                nameList.add(name0 != null ? name0 : "");
            }
            if (number1 != null && !number1.trim().isEmpty()) {
                numList.add(number1);
                nameList.add(name1 != null ? name1 : "");
            }

            String[] numbers = numList.toArray(new String[0]);
            String[] names = nameList.toArray(new String[0]);
            //checkbox de contagem como "on"
            boolean counter = "on".equalsIgnoreCase(counterStr);
            //gravar o grupo usando o service
            groupService.saveGroup(groupNumberStr, numbers, names, counter);
            //definir a cookie como ultimo grupo modificado
            response.setHeader("Set-Cookie", "LastGroupNumber=" + groupNumber + ";Path=/");
           //responder com um JSON de sucesso
            String successResponse = "{\"message\":\"Grupo guardado com sucesso!\"}";
            response.setCode(ReplyCode.OK);
            response.setHeader("Content-Type", "application/json; charset=UTF-8");
            response.setHeader("Content-Length", String.valueOf(successResponse.length()));
            String connectionHeader = request.headers.getHeaderValue("Connection");
            if ("keep-alive".equalsIgnoreCase(connectionHeader)) {
                response.setHeader("Connection", "keep-alive");
            } else {
                response.setHeader("Connection", "close");
            }
            response.setText(successResponse);
            } catch (Exception e) {
            logger.error("Erro ao gravar o grupo", e);
            String errorResponse = "{\"error\":\"Erro interno ao processar o formulário\"}";
            response.setCode(ReplyCode.BADREQ);
            response.setHeader("Content-Type", "application/json; charset=UTF-8");
            response.setHeader("Content-Length", String.valueOf(errorResponse.length()));
            String connectionHeader = request.headers.getHeaderValue("Connection");
            if ("keep-alive".equalsIgnoreCase(connectionHeader)) {
                response.setHeader("Connection", "keep-alive");
            } else {
                response.setHeader("Connection", "close");
            }
            response.setText(errorResponse);
        }
    }

    @Override
    protected void handleDelete(Request request, Response response) {
        logger.debug("DELETE /api - Deleting group");
        
        try {
            // Extract groupNumber from URL (e.g., /api?groupNumber=42)
            String urlText = request.urlText;
            String groupNumberStr = null;
            
            if (urlText.contains("?")) {
                String queryString = urlText.substring(urlText.indexOf("?") + 1);
                String[] params = queryString.split("&");
                for (String param : params) {
                    if (param.startsWith("groupNumber=")) {
                        groupNumberStr = param.substring("groupNumber=".length());
                        try {
                            groupNumberStr = java.net.URLDecoder.decode(groupNumberStr, "UTF-8");
                        } catch (Exception e) {
                            logger.debug("Error decoding query parameter", e);
                        }
                        break;
                    }
                }
            }
            
            // Valida o groupNumber
            if (groupNumberStr == null || groupNumberStr.trim().isEmpty()) {
                String errorResponse = "{\"error\":\"O número do grupo é obrigatório\"}";
                response.setCode(ReplyCode.BADREQ);
                response.setHeader("Content-Type", "application/json; charset=UTF-8");
                response.setHeader("Content-Length", String.valueOf(errorResponse.length()));
                String connectionHeader = request.headers.getHeaderValue("Connection");
                if ("keep-alive".equalsIgnoreCase(connectionHeader)) {
                    response.setHeader("Connection", "keep-alive");
                } else {
                    response.setHeader("Connection", "close");
                }
                response.setText(errorResponse);
                return;
            }
            
            //Apagar o grupo
            groupService.deleteGroup(groupNumberStr);
            
            // Retorna success 
            String successResponse = "{\"message\":\"Grupo eliminado com sucesso!\"}";
            response.setCode(ReplyCode.OK);
            response.setHeader("Content-Type", "application/json; charset=UTF-8");
            response.setHeader("Content-Length", String.valueOf(successResponse.length()));
            String connectionHeader = request.headers.getHeaderValue("Connection");
            if ("keep-alive".equalsIgnoreCase(connectionHeader)) {
                response.setHeader("Connection", "keep-alive");
            } else {
                response.setHeader("Connection", "close");
            }
            response.setText(successResponse);
            logger.info("DELETE /api - Group {} deleted successfully", groupNumberStr);
        } catch (Exception e) {
            logger.error("Error deleting group", e);
            String errorResponse = "{\"error\":\"Erro ao eliminar o grupo\"}";
            response.setCode(ReplyCode.BADREQ);
            response.setHeader("Content-Type", "application/json; charset=UTF-8");
            response.setHeader("Content-Length", String.valueOf(errorResponse.length()));
            String connectionHeader = request.headers.getHeaderValue("Connection");
            if ("keep-alive".equalsIgnoreCase(connectionHeader)) {
                response.setHeader("Connection", "keep-alive");
            } else {
                response.setHeader("Connection", "close");
            }
            response.setText(errorResponse);
        }
    }
}
